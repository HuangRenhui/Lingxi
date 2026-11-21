package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.ConnectorCreateRequest;
import com.lingxi.dto.ConnectorUpdateRequest;
import com.lingxi.entity.AiMcpServer;
import com.lingxi.mapper.AiMcpServerMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.ConnectorService;
import com.lingxi.vo.ConnectorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 连接器基础服务实现。
 */
@Service
@RequiredArgsConstructor
public class ConnectorServiceImpl implements ConnectorService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    private final AiMcpServerMapper mcpServerMapper;

    @Override
    public List<ConnectorVO> listMine(int page, int size) {
        // 页码与页大小做边界收敛，避免异常参数打到数据库
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<AiMcpServer> wrapper = new LambdaQueryWrapper<AiMcpServer>()
                .eq(AiMcpServer::getUserId, SecurityUtils.getUserId())
                .orderByDesc(AiMcpServer::getId);
        Page<AiMcpServer> result = mcpServerMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ConnectorVO get(Long id) {
        return toVO(getOwnedConnector(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConnectorVO create(ConnectorCreateRequest request) {
        AiMcpServer server = new AiMcpServer();
        server.setUserId(SecurityUtils.getUserId());
        server.setName(request.getName().trim());
        server.setDescription(trimToNull(request.getDescription()));
        // 未指定接入类型时按 HTTP 处理
        server.setServerType(StringUtils.hasText(request.getServerType()) ? request.getServerType() : "HTTP");
        server.setEndpoint(trimToNull(request.getEndpoint()));
        validateEndpoint(server.getServerType(), server.getEndpoint());
        // 新建连接器默认未连接，由前端连接操作切换状态
        server.setStatus(0);
        mcpServerMapper.insert(server);
        // 重新查询以拿到数据库生成的创建/更新时间
        return toVO(mcpServerMapper.selectById(server.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConnectorVO update(Long id, ConnectorUpdateRequest request) {
        AiMcpServer server = getOwnedConnector(id);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            server.setName(name);
        }
        if (request.getDescription() != null) {
            server.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getServerType() != null) {
            server.setServerType(request.getServerType());
        }
        if (request.getEndpoint() != null) {
            server.setEndpoint(trimToNull(request.getEndpoint()));
        }
        if (request.getStatus() != null) {
            server.setStatus(request.getStatus());
        }
        validateEndpoint(server.getServerType(), server.getEndpoint());

        // 用 UpdateWrapper 显式写入 null，保证「传入空串清空简介/地址」真实生效
        LambdaUpdateWrapper<AiMcpServer> update =
                new LambdaUpdateWrapper<AiMcpServer>().eq(AiMcpServer::getId, id);
        boolean changed = false;
        if (request.getName() != null) {
            update.set(AiMcpServer::getName, server.getName());
            changed = true;
        }
        if (request.getDescription() != null) {
            update.set(AiMcpServer::getDescription, server.getDescription());
            changed = true;
        }
        if (request.getServerType() != null) {
            update.set(AiMcpServer::getServerType, server.getServerType());
            changed = true;
        }
        if (request.getEndpoint() != null) {
            update.set(AiMcpServer::getEndpoint, server.getEndpoint());
            changed = true;
        }
        if (request.getStatus() != null) {
            update.set(AiMcpServer::getStatus, server.getStatus());
            changed = true;
        }
        if (!changed) {
            return toVO(server);
        }
        mcpServerMapper.update(null, update);
        return toVO(mcpServerMapper.selectById(id));
    }

    /**
     * 校验连接地址：STDIO 为启动命令，禁止控制字符与 shell 元字符；HTTP/SSE 必须是 http(s) 地址。
     */
    private void validateEndpoint(String serverType, String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            return;
        }
        if ("STDIO".equals(serverType)) {
            // 换行/回车/Tab 等控制字符可绕过原来的黑名单形成命令分隔，必须一并拒绝
            if (endpoint.matches("(?s).*[\\u0000-\\u001F\\u007F;&|`$><].*")) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "STDIO 命令包含非法字符");
            }
            return;
        }
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "HTTP/SSE 地址必须以 http:// 或 https:// 开头");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先校验归属，避免越权删除
        getOwnedConnector(id);
        mcpServerMapper.deleteById(id);
    }

    /**
     * 查询连接器并校验归属。
     */
    private AiMcpServer getOwnedConnector(Long id) {
        AiMcpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.CONNECTOR_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(server.getUserId())) {
            throw new BusinessException(ResultCode.CONNECTOR_NOT_OWNED);
        }
        return server;
    }

    /**
     * 空字符串归一为 null，避免前端把可空字段传成空串。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 实体转视图。
     */
    private ConnectorVO toVO(AiMcpServer server) {
        ConnectorVO vo = new ConnectorVO();
        vo.setId(server.getId());
        vo.setName(server.getName());
        vo.setDescription(server.getDescription());
        vo.setServerType(server.getServerType());
        vo.setEndpoint(server.getEndpoint());
        vo.setStatus(server.getStatus());
        vo.setCreatedAt(server.getCreatedAt());
        vo.setUpdatedAt(server.getUpdatedAt());
        return vo;
    }
}
