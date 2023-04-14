package zerobase.bud.common.exception;

import static zerobase.bud.common.type.ErrorCode.AWS_S3_ERROR;
import static zerobase.bud.common.type.ErrorCode.INTERNAL_ERROR;
import static zerobase.bud.common.type.ErrorCode.REQUEST_METHOD_OR_URL_ERROR;
import static zerobase.bud.common.type.ErrorCode.WRONG_REQUEST_TYPE_ERROR;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zerobase.bud.common.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException he
    ) {
        log.error("HttpRequestMethodNotSupportedException is occurred", he);
        return ResponseEntity.internalServerError().body(ErrorResponse.builder()
            .errorCode(REQUEST_METHOD_OR_URL_ERROR)
            .message(REQUEST_METHOD_OR_URL_ERROR.getDescription())
            .build());
    }

    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<ErrorResponse> handleAmazonS3Exception(
        AmazonS3Exception ae) {
        log.error("AWS S3 Exception is occurred", ae);
        return ResponseEntity.internalServerError().body(ErrorResponse.builder()
            .errorCode(AWS_S3_ERROR)
            .message(ae.getMessage())
            .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException he) {
        log.error("HttpMessageNotReadableException is occurred {}",
            he.getMessage());
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .errorCode(WRONG_REQUEST_TYPE_ERROR)
                .message(he.getMessage())
                .build()
        );
    }

    @ExceptionHandler(BudException.class)
    public ResponseEntity<ErrorResponse> handleBudException(BudException e) {
        log.error("{} is occurred", e.getErrorCode());
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(
        MemberException e) {
        log.error("{} is occurred", e.getErrorCode());
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(ChatRoomException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomException(
        ChatRoomException e) {
        log.error("{} is occurred", e.getErrorCode());
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomException(
        ChatException e) {
        log.error("{} is occurred", e.getErrorCode());
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception is occurred", e);
        return ResponseEntity.internalServerError().body(
            ErrorResponse.builder()
                .errorCode(INTERNAL_ERROR)
                .message(INTERNAL_ERROR.getDescription())
                .build()
        );
    }
}
