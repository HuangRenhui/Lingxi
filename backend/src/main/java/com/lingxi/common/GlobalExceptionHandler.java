package com.lingxi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
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

/**
 * 全局异常处理：业务异常与参数校验异常统一转换为 Result。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // @RequestBody 上的 Bean Validation 失败：只返回第一条字段错误，避免把完整校验细节暴露给调用方。
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        // 表单/查询参数绑定失败，处理方式与 @RequestBody 校验保持一致。
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        // 方法参数上的约束校验（如 @Validated + @NotBlank）失败时走到这里。
        return Result.error(ResultCode.PARAM_ERROR.getCode(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        // 请求体缺失、JSON 语法错误或字段类型无法反序列化：属于客户端请求错误，不再落入 5000。
        log.warn("Request body not readable: {}", ex.getMessage());
        return Result.error(ResultCode.REQUEST_BODY_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // 路径变量或查询参数类型不匹配（如 id 传入非数字）时给出明确的参数错误。
        return Result.error(ResultCode.PARAM_TYPE_MISMATCH.getCode(),
                "参数 " + ex.getName() + " 类型不匹配");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        // 405 不应落入兜底的 5000，否则监控会把客户端误用当成服务故障
        log.warn("Method not supported: {} {}", ex.getMethod(), ex.getMessage());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return Result.error(ResultCode.API_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException ex) {
        // 唯一索引冲突（如逻辑删除后重建同名记录）应返回明确的业务错误
        log.warn("Duplicate key: {}", ex.getMostSpecificCause().getMessage());
        return Result.error(ResultCode.DATA_CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // 字段超长、非空约束等数据层校验失败归为参数错误
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "提交的数据不符合字段约束");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        // 兜底异常：完整堆栈只写入服务端日志，对外统一返回通用错误，避免泄露实现细节。
        log.error("Unhandled exception", ex);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}
