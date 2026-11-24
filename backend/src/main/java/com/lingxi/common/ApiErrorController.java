package com.lingxi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 统一错误响应。
 *
 * <p>为什么需要它：项目未关闭静态资源映射，Spring Boot 会为 {@code /**} 注册
 * {@code ResourceHttpRequestHandler}，因此 DispatcherServlet 永远能匹配到「处理器」，
 * {@code NoHandlerFoundException} 不会抛出——{@code spring.mvc.throw-exception-if-handler-not-found}
 * 与对应的异常处理器实际是死代码。容器最终通过 {@code sendError} 转发到 {@code /error}，
 * 默认由 {@code BasicErrorController} 输出 Spring 自有结构（无 {@code code} 字段），
 * 前端拦截器无法解析。这里接管 {@code /error}，保证所有错误都返回统一的 Result 结构。</p>
 */
@Slf4j
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Result<Void>> handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr instanceof Integer
                ? (Integer) statusAttr
                : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String path = String.valueOf(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        ResultCode code = resolveCode(status);
        if (status >= 500) {
            // 5xx 需要保留可追踪的日志，路径用于定位出错的接口
            log.error("Request failed with status {}: {}", status, path);
        } else {
            log.warn("Request failed with status {}: {}", status, path);
        }
        return ResponseEntity.status(status).body(Result.error(code));
    }

    private ResultCode resolveCode(int status) {
        switch (status) {
            case 400:
                return ResultCode.PARAM_ERROR;
            case 401:
                return ResultCode.UNAUTHORIZED;
            case 403:
                return ResultCode.FORBIDDEN;
            case 404:
                return ResultCode.API_NOT_FOUND;
            case 405:
                return ResultCode.METHOD_NOT_ALLOWED;
            default:
                return status >= 500 ? ResultCode.INTERNAL_ERROR : ResultCode.FAIL;
        }
    }
}
