package zerobase.bud.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import zerobase.bud.common.type.ErrorCode;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;
}
