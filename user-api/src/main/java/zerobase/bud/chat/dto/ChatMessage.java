package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public class ChatMessage {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        private Long senderId;
        @NotNull
        private Long chatroomId;
        @NotBlank
        private String message;
    }
}
