package zerobase.bud.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zerobase.bud.dto.ErrorResponse;
import zerobase.bud.type.ErrorCode;

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

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException e) {
        log.error("{} is occurred", e.getErrorCode());
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .errorCode(e.getErrorCode())
                        .message(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ChatRoomException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomException(ChatRoomException e) {
        log.error("{} is occurred", e.getErrorCode());
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .errorCode(e.getErrorCode())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomException(ChatException e) {
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
                        .errorCode(ErrorCode.INTERNAL_ERROR)
                        .message(ErrorCode.INTERNAL_ERROR.getDescription())
                        .build()
        );
    }
}
