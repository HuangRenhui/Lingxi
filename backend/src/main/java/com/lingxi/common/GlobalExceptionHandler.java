package com.lingxi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 全局异常处理：业务异常与参数校验异常统一转换为 Result。
 *
 * <p>HTTP 状态码与业务语义保持一致：参数错误 400、未登录 401、无权限 403、
 * 资源不存在 404、状态冲突 409、系统错误 500。这样网关、监控与浏览器开发者工具
 * 无需解析响应体即可识别故障类型；响应体仍保留原有业务 code，前端逻辑不受影响。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 资源不存在类业务码 → 404。 */
    private static final Set<Integer> NOT_FOUND_CODES = new HashSet<>(Arrays.asList(
            ResultCode.USER_NOT_FOUND.getCode(),
            ResultCode.TASK_NOT_FOUND.getCode(),
            ResultCode.APPLICATION_NOT_FOUND.getCode(),
            ResultCode.API_KEY_NOT_FOUND.getCode(),
            ResultCode.NOTIFICATION_NOT_FOUND.getCode(),
            ResultCode.DEVICE_NOT_FOUND.getCode(),
            ResultCode.SKILL_NOT_FOUND.getCode(),
            ResultCode.CONNECTOR_NOT_FOUND.getCode(),
            ResultCode.ARTIFACT_NOT_FOUND.getCode(),
            ResultCode.ORDER_NOT_FOUND.getCode(),
            ResultCode.WALLET_NOT_FOUND.getCode(),
            ResultCode.SUBSCRIPTION_NOT_FOUND.getCode(),
            ResultCode.SUBSCRIPTION_PLAN_NOT_FOUND.getCode(),
            ResultCode.COUPON_NOT_FOUND.getCode(),
            ResultCode.REDEEM_CODE_NOT_FOUND.getCode(),
            ResultCode.BILLING_RULE_NOT_FOUND.getCode(),
            ResultCode.MODERATION_REPORT_NOT_FOUND.getCode(),
            ResultCode.SENSITIVE_WORD_NOT_FOUND.getCode()));

    /** 越权/状态不允许类业务码 → 403。 */
    private static final Set<Integer> FORBIDDEN_CODES = new HashSet<>(Arrays.asList(
            ResultCode.FORBIDDEN.getCode(),
            ResultCode.ACCOUNT_DISABLED.getCode(),
            ResultCode.TASK_NOT_OWNED.getCode(),
            ResultCode.APPLICATION_NOT_OWNED.getCode(),
            ResultCode.API_KEY_NOT_OWNED.getCode(),
            ResultCode.NOTIFICATION_NOT_OWNED.getCode(),
            ResultCode.DEVICE_NOT_OWNED.getCode(),
            ResultCode.SKILL_NOT_OWNED.getCode(),
            ResultCode.CONNECTOR_NOT_OWNED.getCode(),
            ResultCode.ARTIFACT_NOT_OWNED.getCode(),
            ResultCode.ORDER_NOT_OWNED.getCode(),
            ResultCode.ADMIN_REQUIRED.getCode(),
            ResultCode.SELF_OPERATION_FORBIDDEN.getCode()));

    /** 状态或唯一约束冲突类业务码 → 409。 */
    private static final Set<Integer> CONFLICT_CODES = new HashSet<>(Arrays.asList(
            ResultCode.DATA_CONFLICT.getCode(),
            ResultCode.TASK_CONCURRENT_MODIFIED.getCode(),
            ResultCode.ORDER_STATUS_INVALID.getCode(),
            ResultCode.ORDER_NOT_EDITABLE.getCode(),
            ResultCode.INSUFFICIENT_BALANCE.getCode(),
            ResultCode.SUBSCRIPTION_PLAN_DISABLED.getCode(),
            ResultCode.USERNAME_TAKEN.getCode(),
            ResultCode.CANNOT_DELETE_SELF.getCode(),
            ResultCode.REDEEM_CODE_ALREADY_USED.getCode(),
            ResultCode.SUBSCRIPTION_PLAN_KEY_TAKEN.getCode()));

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        return build(httpStatusFor(ex.getCode()), ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // @RequestBody 上的 Bean Validation 失败：只返回第一条字段错误，避免把完整校验细节暴露给调用方。
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException ex) {
        // 表单/查询参数绑定失败，处理方式与 @RequestBody 校验保持一致。
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        // 方法参数上的约束校验（如 @Validated + @NotBlank）失败时走到这里。
        return build(HttpStatus.BAD_REQUEST, ResultCode.PARAM_ERROR.getCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        // 请求体缺失、JSON 语法错误或字段类型无法反序列化：属于客户端请求错误，不再落入 5000。
        log.warn("Request body not readable: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ResultCode.REQUEST_BODY_ERROR.getCode(),
                ResultCode.REQUEST_BODY_ERROR.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // 路径变量或查询参数类型不匹配（如 id 传入非数字）时给出明确的参数错误。
        return build(HttpStatus.BAD_REQUEST, ResultCode.PARAM_TYPE_MISMATCH.getCode(),
                "参数 " + ex.getName() + " 类型不匹配");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        // 405 不应落入兜底的 5000，否则监控会把客户端误用当成服务故障
        log.warn("Method not supported: {} {}", ex.getMethod(), ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, ResultCode.METHOD_NOT_ALLOWED.getCode(),
                ResultCode.METHOD_NOT_ALLOWED.getMessage());
    }

    /**
     * 注意：仅在关闭静态资源映射（spring.web.resources.add-mappings=false）时才会命中。
     * 默认配置下 {@code /**} 由 ResourceHttpRequestHandler 接管，404 通过 sendError 转发到
     * {@code /error}，由 {@link ApiErrorController} 输出统一结构。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return build(HttpStatus.NOT_FOUND, ResultCode.API_NOT_FOUND.getCode(),
                ResultCode.API_NOT_FOUND.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        // 唯一索引冲突（如逻辑删除后重建同名记录）应返回明确的业务错误；
        // 带上异常对象以便定位具体索引，否则只有一句 SQL 报错无法排查
        log.warn("Duplicate key: {}", ex.getMostSpecificCause().getMessage(), ex);
        return build(HttpStatus.CONFLICT, ResultCode.DATA_CONFLICT.getCode(),
                ResultCode.DATA_CONFLICT.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // 字段超长、非空约束等数据层校验失败归为参数错误
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage(), ex);
        return build(HttpStatus.BAD_REQUEST, ResultCode.PARAM_ERROR.getCode(), "提交的数据不符合字段约束");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        // 兜底异常：完整堆栈只写入服务端日志，对外统一返回通用错误，避免泄露实现细节。
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ResultCode.INTERNAL_ERROR.getCode(),
                ResultCode.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<Result<Void>> build(HttpStatus status, int code, String message) {
        return ResponseEntity.status(status).body(Result.error(code, message));
    }

    /**
     * 业务码映射到 HTTP 状态；未归类的业务码一律按客户端请求错误（400）处理。
     */
    private HttpStatus httpStatusFor(int code) {
        if (code == ResultCode.UNAUTHORIZED.getCode() || code == ResultCode.INVALID_CREDENTIALS.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code == ResultCode.LOGIN_LOCKED.getCode()) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (NOT_FOUND_CODES.contains(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if (FORBIDDEN_CODES.contains(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if (CONFLICT_CODES.contains(code)) {
            return HttpStatus.CONFLICT;
        }
        if (code == ResultCode.INTERNAL_ERROR.getCode() || code == ResultCode.FAIL.getCode()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
