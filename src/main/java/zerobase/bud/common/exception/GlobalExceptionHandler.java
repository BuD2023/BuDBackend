package zerobase.bud.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zerobase.bud.common.dto.ErrorResponse;
import zerobase.bud.common.type.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception e) {
        log.error("Exception is occurred", e);
        return new ResponseEntity<>(ErrorResponse.of(ErrorCode.INTERNAL_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
