package zerobase.bud.post.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UpdateQnaAnswer {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotNull
        @Min(1)
        private Long qnaAnswerId;

        @NotNull
        private String content;

    }
}
