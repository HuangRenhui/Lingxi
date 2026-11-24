package com.lingxi.security;

import com.lingxi.common.Result;
import com.lingxi.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 未认证访问受保护接口时，返回统一 JSON（HTTP 401 + code 401）。
 *
 * <p>HTTP 状态与业务码保持一致：网关、监控与浏览器的 Network 面板都需要能直接识别未认证，
 * 而不仅仅是靠响应体里的 code。前端拦截器同时处理 HTTP 401 与 code=401 两种入口。</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = buildObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(ResultCode.UNAUTHORIZED));
    }

    private static ObjectMapper buildObjectMapper() {
        // 该 Handler 运行在 Spring MVC 的消息转换器之外，因此单独构建 ObjectMapper；
        // 注册 JavaTimeModule 以保证 Result.timestamp 按 ISO 格式输出而非时间戳数组。
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
