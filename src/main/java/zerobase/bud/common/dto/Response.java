package zerobase.bud.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.common.type.ResponseCode;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> {
    private int status;
    private String message;
    @JsonInclude(Include.NON_NULL)
    private T data;

    public static <T> Response<T> of(ResponseCode responseCode, T data) {
        return Response.<T>builder()
                .status(responseCode.getHttpStatus().value())
                .message(responseCode.getDescription())
                .data(data)
                .build();
    }

    public static Response of(ResponseCode responseCode) {
        return Response.builder()
                .status(responseCode.getHttpStatus().value())
                .message(responseCode.getDescription())
                .build();
    }
}
