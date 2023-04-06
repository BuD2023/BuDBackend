package zerobase.bud.common.exception;

import lombok.*;
import zerobase.bud.common.type.ErrorCode;

@Getter
@Setter
public class ChatRoomException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;

    public ChatRoomException(ErrorCode errorCode){
        this.errorCode = errorCode;
        this.message = errorCode.getDescription();
    }
}
