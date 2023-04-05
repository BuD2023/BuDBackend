package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;

import java.util.List;

import static zerobase.bud.common.type.ErrorCode.CHATROOM_NOT_FOUND;
import static zerobase.bud.common.util.Constant.*;
import static zerobase.bud.type.ChatRoomStatus.ACTIVE;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRepository chatRepository;

    @Transactional
    public Long createChatRoom(String title, String description, List<String> hashTag) {

        String hastStr = String.join("#", hashTag);

        return chatRoomRepository.save(
                ChatRoom.builder()
                        .title(title)
                        .status(ACTIVE)
                        .hashTag(hastStr)
                        .description(description)
                        .build()).getId();
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDto> searchChatRooms(String keyword, int page) {
        return chatRoomRepository
                .findAllByTitleContainingIgnoreCaseAndStatus(keyword, ACTIVE,
                        PageRequest.of(page, CHATROOM_SIZE_PER_PAGE))
                .map(ChatRoomDto::from);
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDto> readChatRooms(int page) {
        return chatRoomRepository.findAllByStatus(ACTIVE,
                        PageRequest.of(page, CHATROOM_SIZE_PER_PAGE))
                .map(ChatRoomDto::from);
    }

    @Transactional(readOnly = true)
    public ChatRoomDto readChatRoom(Long chatroomId) {
        return ChatRoomDto.from(
                chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                        .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND))
        );
    }

    @Transactional(readOnly = true)
    public Slice<ChatDto> readChats(Long chatroomId, int page) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));

        return chatRepository.findAllByChatRoomOrderByCreatedAtDesc(chatRoom,
                PageRequest.of(page, CHAT_SIZE_PER_PAGE))
                .map(ChatDto::from);
    }
}
