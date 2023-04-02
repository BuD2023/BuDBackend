package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.ChatRoom;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private Long chatRoomId;
    private String title;
    private int numberOfMembers;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .title(chatRoom.getTitle())
                .numberOfMembers(chatRoom.getNumberOfMembers())
                .build();
    }
}
