package zerobase.bud.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zerobase.bud.common.dto.ErrorResponse;
import zerobase.bud.common.type.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {
        log.error("Exception is occurred", e);
        return new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
    }
}
