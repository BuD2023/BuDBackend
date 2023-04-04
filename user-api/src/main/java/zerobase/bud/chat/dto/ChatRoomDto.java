package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.ChatRoom;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private Long chatRoomId;
    private String title;
    private int numberOfMembers;
    private String description;
    private List<String> hashTags;
    private LocalDateTime createdAt;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .title(chatRoom.getTitle())
                .numberOfMembers(chatRoom.getNumberOfMembers())
                .description(chatRoom.getDescription())
                .hashTags(Arrays.asList(chatRoom.getHashTag().split("#")))
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
