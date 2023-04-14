package zerobase.bud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.Chat;
import zerobase.bud.type.ChatType;
import zerobase.bud.util.TimeUtil;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatDto implements Serializable {
    private Long chatroomId;
    private Long chatId;
    private String message;
    private ChatType chatType;
    private String createdAt;
    private String imageUrl;
    private String userProfileUrl;
    private String userName;
    private int numberOfMembers;
    private Long userId;

    public static ChatDto from(Chat chat) {
        return ChatDto.builder()
                .chatroomId(chat.getChatRoom().getId())
                .chatId(chat.getId())
                .chatType(chat.getType())
                .createdAt(TimeUtil.caculateTerm(chat.getCreatedAt()))
                .message(chat.getMessage())
                .userName(chat.getMember().getNickname())
                .userProfileUrl(chat.getMember().getProfileImg())
                .userId(chat.getId())
                .build();
    }

    public static ChatDto of(ChatType chatType, Long chatroomId, Integer numberOfMembers) {
        return ChatDto.builder()
                .chatType(chatType)
                .chatroomId(chatroomId)
                .numberOfMembers(numberOfMembers)
                .build();
    }
}
