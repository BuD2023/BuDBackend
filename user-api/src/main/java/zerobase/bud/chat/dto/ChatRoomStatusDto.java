package zerobase.bud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomStatusDto {
    long numberOfChatRooms;
    long numberOfUsers;

    public static ChatRoomStatusDto of(long numberOfChatRooms, long numberOfUsers) {
        return ChatRoomStatusDto.builder()
                .numberOfChatRooms(numberOfChatRooms)
                .numberOfUsers(numberOfUsers)
                .build();
    }
}
