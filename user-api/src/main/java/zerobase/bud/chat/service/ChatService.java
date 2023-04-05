package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Chat;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.type.ChatType;

import static zerobase.bud.type.ChatRoomStatus.ACTIVE;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatDto chatting(String message, Long roomId, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(roomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(ErrorCode.CHATROOM_NOT_FOUND));
        return ChatDto.from(
                chatRepository.save(
                        Chat.builder()
                                .chatRoom(chatRoom)
                                .message(message)
                                .type(ChatType.MESSAGE)
                                .build())
        );
    }
}
