package zerobase.bud.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    TEST_OK(HttpStatus.OK, "설명입니다");
    private final HttpStatus httpStatus;
    private final String description;
}
