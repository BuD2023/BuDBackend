package zerobase.bud.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.common.util.TimeUtil;
import zerobase.bud.domain.Chat;
import zerobase.bud.type.ChatType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDto {
    private Long chatId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    private ChatType chatType;
    private String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String imageUrl;

    public static ChatDto from(Chat chat) {
        return ChatDto.builder()
                .chatId(chat.getId())
                .chatType(chat.getType())
                .createdAt(TimeUtil.caculateTerm(chat.getCreatedAt()))
                .message(chat.getMessage())
                .build();
    }
}
