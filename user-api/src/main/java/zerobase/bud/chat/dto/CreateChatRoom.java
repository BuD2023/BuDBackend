package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CreateChatRoom {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank
        @Size(min = 1, max = 50)
        private String title;

        @NotBlank
        @Size(min = 1, max = 255)
        private String description;

        @Builder.Default
        private List<String> hashTag = new ArrayList<>();
    }
}
