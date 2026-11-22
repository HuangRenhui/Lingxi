package com.lingxi.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 业务码到 HTTP 状态码的映射回归：确保网关/监控能按状态码识别故障类型，
 * 同时响应体仍保留原有业务 code，避免破坏前端既有判断。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpStatus statusOf(ResultCode resultCode) {
        ResponseEntity<Result<Void>> response =
                handler.handleBusinessException(new BusinessException(resultCode));
        Result<Void> body = response.getBody();
        assertEquals(resultCode.getCode(), body.getCode());
        assertFalse(body.getSuccess());
        return response.getStatusCode();
    }

    @Test
    void mapsNotFoundCodesTo404() {
        assertEquals(HttpStatus.NOT_FOUND, statusOf(ResultCode.COUPON_NOT_FOUND));
        assertEquals(HttpStatus.NOT_FOUND, statusOf(ResultCode.SENSITIVE_WORD_NOT_FOUND));
        assertEquals(HttpStatus.NOT_FOUND, statusOf(ResultCode.USER_NOT_FOUND));
    }

    @Test
    void mapsCredentialCodesTo401() {
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf(ResultCode.INVALID_CREDENTIALS));
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf(ResultCode.UNAUTHORIZED));
    }

    @Test
    void mapsOwnershipCodesTo403() {
        assertEquals(HttpStatus.FORBIDDEN, statusOf(ResultCode.SELF_OPERATION_FORBIDDEN));
        assertEquals(HttpStatus.FORBIDDEN, statusOf(ResultCode.ADMIN_REQUIRED));
        assertEquals(HttpStatus.FORBIDDEN, statusOf(ResultCode.TASK_NOT_OWNED));
    }

    @Test
    void mapsConflictCodesTo409() {
        assertEquals(HttpStatus.CONFLICT, statusOf(ResultCode.DATA_CONFLICT));
        assertEquals(HttpStatus.CONFLICT, statusOf(ResultCode.SUBSCRIPTION_PLAN_KEY_TAKEN));
        assertEquals(HttpStatus.CONFLICT, statusOf(ResultCode.REDEEM_CODE_ALREADY_USED));
    }

    @Test
    void mapsServerCodesTo500() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusOf(ResultCode.INTERNAL_ERROR));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusOf(ResultCode.FAIL));
    }

    @Test
    void mapsLoginLockTo429() {
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, statusOf(ResultCode.LOGIN_LOCKED));
    }

    @Test
    void mapsUnclassifiedCodesTo400() {
        assertEquals(HttpStatus.BAD_REQUEST, statusOf(ResultCode.PARAM_ERROR));
    }

    @Test
    void keepsCustomMessageForUnknownBusinessCode() {
        ResponseEntity<Result<Void>> response =
                handler.handleBusinessException(new BusinessException(9999, "自定义错误"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("自定义错误", response.getBody().getMessage());
    }
}
