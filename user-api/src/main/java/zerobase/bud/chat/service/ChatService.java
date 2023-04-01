package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.repository.ChatRoomRepository;


@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public Long createChatRoom(String title) {
        return chatRoomRepository.save(
                        ChatRoom.builder().title(title).build())
                .getId();
    }
}
