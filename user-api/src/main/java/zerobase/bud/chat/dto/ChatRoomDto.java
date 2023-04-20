package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.ChatRoom;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private Long chatRoomId;
    private String title;
    private long numberOfMembers;
    private String description;
    private List<String> hashTags;
    private LocalDateTime createdAt;
    private String hostName;
    private String hostProfileUrl;
    private Long hostId;

    public static ChatRoomDto of(ChatRoom chatRoom, Long numberOfMembers) {
        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .title(chatRoom.getTitle())
                .hostName(chatRoom.getMember().getNickname())
                .hostProfileUrl(chatRoom.getMember().getProfileImg())
                .description(chatRoom.getDescription())
                .hashTags(
                        Arrays.stream(chatRoom.getHashTag().split("#"))
                                .filter(hashTag -> !Objects.equals(hashTag, ""))
                                .collect(Collectors.toList())
                )
                .createdAt(chatRoom.getCreatedAt())
                .numberOfMembers(numberOfMembers)
                .hostId(chatRoom.getMember().getId())
                .build();
    }
}
