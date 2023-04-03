package zerobase.bud.common.exception;

import static zerobase.bud.type.ErrorCode.INTERNAL_ERROR;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zerobase.bud.dto.ErrorResponse;
import zerobase.bud.exception.BudException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
