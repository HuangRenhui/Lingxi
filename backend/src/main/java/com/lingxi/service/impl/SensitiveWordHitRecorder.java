package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingxi.entity.ModerationSensitiveWord;
import com.lingxi.mapper.ModerationSensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 敏感词命中计数。
 *
 * <p>独立 Bean + REQUIRES_NEW：命中后调用方会抛出业务异常导致外层事务回滚，
 * 命中次数必须单独提交才能保留，否则统计永远为 0。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordHitRecorder {

    private final ModerationSensitiveWordMapper wordMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordHit(Long wordId) {
        if (wordId == null) {
            return;
        }
        try {
            wordMapper.update(null, new LambdaUpdateWrapper<ModerationSensitiveWord>()
                    .eq(ModerationSensitiveWord::getId, wordId)
                    .setSql("hits = hits + 1"));
        } catch (RuntimeException ex) {
            // 计数只是辅助指标，写入失败不应影响拦截结果
            log.warn("敏感词命中计数失败: wordId={}", wordId, ex);
        }
    }
}
