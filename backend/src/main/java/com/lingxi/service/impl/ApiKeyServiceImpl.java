package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.ApiKeyCreateRequest;
import com.lingxi.dto.ApiKeyUpdateRequest;
import com.lingxi.entity.ApiKey;
import com.lingxi.mapper.ApiKeyMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.ApiKeyService;
import com.lingxi.vo.ApiKeyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API Key 基础服务实现。
 */
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final int MAX_PAGE_SIZE = 100;
    /** 密钥前缀长度，包含 lx_ 共 12 位 */
    private static final int PREFIX_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyMapper apiKeyMapper;

    @Override
    public List<ApiKeyVO> listMine(int page, int size) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getUserId, SecurityUtils.getUserId())
                .orderByDesc(ApiKey::getId);
        Page<ApiKey> result = apiKeyMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ApiKeyVO get(Long id) {
        return toVO(getOwnedKey(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiKeyVO create(ApiKeyCreateRequest request) {
        String plainKey = generateKey();
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(SecurityUtils.getUserId());
        apiKey.setName(request.getName().trim());
        apiKey.setKeyPrefix(plainKey.substring(0, PREFIX_LENGTH));
        apiKey.setKeyHash(sha256(plainKey));
        apiKey.setStatus(1);
        apiKeyMapper.insert(apiKey);

        ApiKeyVO vo = toVO(apiKeyMapper.selectById(apiKey.getId()));
        // 明文只在此处返回，后续列表与详情均不含密钥
        vo.setKey(plainKey);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiKeyVO update(Long id, ApiKeyUpdateRequest request) {
        ApiKey apiKey = getOwnedKey(id);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            apiKey.setName(name);
        }
        if (request.getStatus() != null) {
            apiKey.setStatus(request.getStatus());
        }

        apiKeyMapper.updateById(apiKey);
        return toVO(apiKeyMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getOwnedKey(id);
        apiKeyMapper.deleteById(id);
    }

    /**
     * 查询密钥并校验归属。
     */
    private ApiKey getOwnedKey(Long id) {
        ApiKey apiKey = apiKeyMapper.selectById(id);
        if (apiKey == null) {
            throw new BusinessException(ResultCode.API_KEY_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(apiKey.getUserId())) {
            throw new BusinessException(ResultCode.API_KEY_NOT_OWNED);
        }
        return apiKey;
    }

    /**
     * 生成 lx_ 前缀的 35 位随机密钥。
     */
    private String generateKey() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        StringBuilder builder = new StringBuilder("lx_");
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    /**
     * 计算 SHA-256 摘要，避免明文落库。
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private ApiKeyVO toVO(ApiKey apiKey) {
        ApiKeyVO vo = new ApiKeyVO();
        vo.setId(apiKey.getId());
        vo.setName(apiKey.getName());
        vo.setKeyPrefix(apiKey.getKeyPrefix());
        vo.setStatus(apiKey.getStatus());
        vo.setCreatedAt(apiKey.getCreatedAt());
        vo.setUpdatedAt(apiKey.getUpdatedAt());
        return vo;
    }
}
