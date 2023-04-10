package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatImageRequest {
    @NotNull
    private Long senderId;
    @NotNull
    private Long chatroomId;
    @NotBlank
    private String imageCode;
}
