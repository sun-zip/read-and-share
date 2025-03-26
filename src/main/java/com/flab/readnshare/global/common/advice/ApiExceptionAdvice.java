package com.flab.readnshare.global.common.advice;

import com.flab.readnshare.global.common.exception.*;
import com.flab.readnshare.global.common.exception.RestTemplateResponseErrorHandler.RestCallException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ErrorResponse response = new ErrorResponse(ex, ErrorCode.INVALID_INPUT_PARAMETER);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Object> handleServerError(Exception ex) {
        ErrorResponse response = new ErrorResponse(ErrorCode.SERVER_ERROR);
        return new ResponseEntity<>(response, ErrorCode.SERVER_ERROR.getStatus());
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity memberExceptionHandler(MemberException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity authExceptionHandler(AuthException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(BookException.class)
    public ResponseEntity bookExceptionHandler(BookException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(FavoriteException.class)
    public ResponseEntity favoriteExceptionHandler(FavoriteException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity reviewExceptionHandler(ReviewException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    @ExceptionHandler(RestCallException.class)
    public ResponseEntity restCallExceptionHandler(RestCallException ex) {
        ErrorResponse response = new ErrorResponse(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(FollowException.class)
    public ResponseEntity followExceptionHandler(FollowException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getStatus());
    }

    // ConstraintViolationException은 @Validated 어노테이션을 사용하여 검증할 때 발생하는 예외
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity constraintViolationExceptionHandler(ConstraintViolationException ex) {
        ErrorResponse response = new ErrorResponse(ErrorCode.INVALID_INPUT_PARAMETER);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // MissingServletRequestParameterException은 필수 파라미터가 누락되었을 때 발생하는 예외
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        ErrorResponse response = new ErrorResponse(ErrorCode.INVALID_INPUT_PARAMETER);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity handlerMethodValidationExceptionHandler(HandlerMethodValidationException ex) {
        ErrorResponse response = new ErrorResponse(ErrorCode.INVALID_INPUT_PARAMETER);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        ErrorResponse response = new ErrorResponse(ErrorCode.INVALID_INPUT_PARAMETER);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleIllegalArgEx(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ReviewException.ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFound(ReviewException.ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("REVIEW_NOT_FOUND", ex.getMessage()));
    }


    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity missingRequestCookieExceptionHandler(MissingRequestCookieException ex) {
        ErrorResponse response = new ErrorResponse(ErrorCode.JWT_NULL);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
