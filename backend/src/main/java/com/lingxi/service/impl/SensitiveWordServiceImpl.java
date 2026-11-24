package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.vo.PageResult;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.SensitiveWordBatchCreateRequest;
import com.lingxi.dto.SensitiveWordUpdateRequest;
import com.lingxi.entity.ModerationSensitiveWord;
import com.lingxi.mapper.ModerationSensitiveWordMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.SensitiveWordService;
import com.lingxi.vo.SensitiveWordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 敏感词管理实现：词库落在服务端，避免只保存在浏览器导致换设备即丢失。
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    /** 允许的分类，非法值归入「其他」，避免脏分类污染筛选。 */
    private static final Set<String> CATEGORIES =
            new LinkedHashSet<>(Arrays.asList("交易", "暴力", "政治", "违法", "隐私", "色情", "其他"));
    private static final Set<String> LEVELS =
            new LinkedHashSet<>(Arrays.asList("高", "中", "低"));

    /** 单页上限与前端取数上限保持一致。 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 内容校验用的词表缓存：写操作会立即失效，未变更时 5 秒内复用。 */
    private static final long WORD_CACHE_TTL_MILLIS = 5_000L;

    private final ModerationSensitiveWordMapper wordMapper;
    private final SensitiveWordHitRecorder hitRecorder;

    private volatile List<ModerationSensitiveWord> cachedWords;
    private volatile long cachedAt;

    @Override
    public PageResult<SensitiveWordVO> list(int page, int size, String keyword, String category) {
        requireAdmin();
        String likeKeyword = StringUtils.hasText(keyword)
                ? keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                : null;
        LambdaQueryWrapper<ModerationSensitiveWord> wrapper = new LambdaQueryWrapper<ModerationSensitiveWord>()
                .like(likeKeyword != null, ModerationSensitiveWord::getWord, likeKeyword)
                .eq(StringUtils.hasText(category) && !"all".equals(category),
                        ModerationSensitiveWord::getCategory, category)
                .orderByDesc(ModerationSensitiveWord::getId);
        long current = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<ModerationSensitiveWord> result = wordMapper.selectPage(new Page<>(current, pageSize), wrapper);
        List<SensitiveWordVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), current, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SensitiveWordVO> batchCreate(SensitiveWordBatchCreateRequest request) {
        requireAdmin();
        String category = normalize(CATEGORIES, request.getCategory(), "其他");
        String level = normalize(LEVELS, request.getLevel(), "中");
        List<String> words = request.getWords().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (words.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请至少输入一个有效敏感词");
        }

        // 词表无唯一索引：先查出已存在的词并跳过，避免批量导入产生重复记录
        Set<String> existing = wordMapper.selectList(new LambdaQueryWrapper<ModerationSensitiveWord>()
                        .in(ModerationSensitiveWord::getWord, words))
                .stream()
                .map(ModerationSensitiveWord::getWord)
                .collect(Collectors.toSet());

        List<SensitiveWordVO> created = new ArrayList<>();
        for (String word : words) {
            if (!existing.add(word)) {
                continue;
            }
            ModerationSensitiveWord entity = new ModerationSensitiveWord();
            entity.setWord(word);
            entity.setCategory(category);
            entity.setLevel(level);
            entity.setHits(0);
            entity.setEnabled(1);
            wordMapper.insert(entity);
            created.add(toVO(entity));
        }
        invalidateCache();
        return created;
    }

    @Override
    public SensitiveWordVO update(Long id, SensitiveWordUpdateRequest request) {
        requireAdmin();
        if (wordMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.SENSITIVE_WORD_NOT_FOUND);
        }
        ModerationSensitiveWord patch = new ModerationSensitiveWord();
        patch.setId(id);
        patch.setEnabled(Boolean.TRUE.equals(request.getEnabled()) ? 1 : 0);
        wordMapper.updateById(patch);
        invalidateCache();
        return toVO(wordMapper.selectById(id));
    }

    @Override
    public void delete(Long id) {
        requireAdmin();
        if (wordMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.SENSITIVE_WORD_NOT_FOUND);
        }
        wordMapper.deleteById(id);
        invalidateCache();
    }

    @Override
    public void checkContent(String... texts) {
        if (texts == null || texts.length == 0) {
            return;
        }
        // 词库规模有限，使用短 TTL 缓存；命中后立刻抛异常，避免继续扫描。
        List<ModerationSensitiveWord> words = enabledWords();
        if (words.isEmpty()) {
            return;
        }
        for (String text : texts) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            // 统一转小写做包含匹配：中文不受影响，英文可覆盖大小写变体。
            String content = text.toLowerCase(Locale.ROOT);
            for (ModerationSensitiveWord word : words) {
                String target = word.getWord() == null ? "" : word.getWord().toLowerCase(Locale.ROOT);
                if (!target.isEmpty() && content.contains(target)) {
                    hitRecorder.recordHit(word.getId());
                    throw new BusinessException(ResultCode.CONTENT_SENSITIVE,
                            "内容包含敏感词「" + word.getWord() + "」，请修改后重试");
                }
            }
        }
    }

    private List<ModerationSensitiveWord> enabledWords() {
        long now = System.currentTimeMillis();
        List<ModerationSensitiveWord> words = cachedWords;
        if (words == null || now - cachedAt > WORD_CACHE_TTL_MILLIS) {
            words = wordMapper.selectList(new LambdaQueryWrapper<ModerationSensitiveWord>()
                    .eq(ModerationSensitiveWord::getEnabled, 1)
                    .orderByAsc(ModerationSensitiveWord::getId));
            cachedWords = words;
            cachedAt = now;
        }
        return words;
    }

    private void invalidateCache() {
        cachedWords = null;
    }

    private String normalize(Set<String> allowed, String value, String fallback) {
        return allowed.contains(value) ? value : fallback;
    }

    private SensitiveWordVO toVO(ModerationSensitiveWord entity) {
        SensitiveWordVO vo = new SensitiveWordVO();
        vo.setId(entity.getId());
        vo.setWord(entity.getWord());
        vo.setCategory(entity.getCategory());
        vo.setLevel(entity.getLevel());
        vo.setHits(entity.getHits() == null ? 0 : entity.getHits());
        vo.setEnabled(Integer.valueOf(1).equals(entity.getEnabled()));
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    /**
     * 仅管理员可访问敏感词管理接口。
     */
    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }
}
