package zerobase.bud.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.common.type.ErrorCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String errorCode;
    private String message;

    public static ErrorResponse of(ErrorCode code) {
        return ErrorResponse.builder()
                .status(code.getHttpStatus().value())
                .errorCode(code.name())
                .message(code.getDescription())
                .build();
    }
}
