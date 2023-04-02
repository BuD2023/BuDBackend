package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.type.ChatRoomStatus;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public Long createChatRoom(String title) {
        return chatRoomRepository.save(
                        ChatRoom.builder().title(title).status(ChatRoomStatus.ACTIVE).build())
                .getId();
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDto> searchChatRoom(String keyword, int page) {
        return chatRoomRepository
                .findAllByTitleContainingIgnoreCaseAndStatus(keyword, ChatRoomStatus.ACTIVE, PageRequest.of(page, 3))
                .map(ChatRoomDto::from);
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDto> getChatRoom(int page) {
        return chatRoomRepository.findAllByStatus(ChatRoomStatus.ACTIVE, PageRequest.of(page, 3))
                .map(ChatRoomDto::from);
    }
}
