package zerobase.bud.exception;

import lombok.Getter;
import lombok.Setter;
import zerobase.bud.type.ErrorCode;

@Getter
@Setter
public class MemberException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    public MemberException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getDescription();
    }
}
