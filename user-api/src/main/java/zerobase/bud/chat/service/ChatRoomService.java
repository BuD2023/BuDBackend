package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.chat.dto.ChatRoomStatusDto;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static zerobase.bud.common.type.ErrorCode.CHATROOM_NOT_FOUND;
import static zerobase.bud.type.ChatRoomStatus.ACTIVE;
import static zerobase.bud.util.Constants.CHATROOM;
import static zerobase.bud.util.Constants.SESSION;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRepository chatRepository;

    private final RedisTemplate<String, Integer> redisTemplate;

    private static ValueOperations<String, Integer> valueOperations;

    public Long createChatRoom(String title, String description, List<String> hashTag, Member member) {

        String hastStr = "";
        if (!hashTag.isEmpty()) {
            hastStr = "#" + String.join("#", hashTag) + "#";
        }

        return chatRoomRepository.save(
                ChatRoom.builder()
                        .title(title)
                        .status(ACTIVE)
                        .hashTag(hastStr)
                        .description(description)
                        .member(member)
                        .build()).getId();
    }

    public Slice<ChatRoomDto> searchChatRooms(String keyword, int page, int size) {

        valueOperations = redisTemplate.opsForValue();

        return chatRoomRepository
                .findByTitleContainsIgnoreCaseOrHashTagContainsIgnoreCaseAndStatusOrderByCreatedAtDesc(
                        keyword, "#" + keyword + "#", ACTIVE, PageRequest.of(page, size))
                .map(chatRoom -> ChatRoomDto.of(chatRoom,
                        getNumberOfMembers(chatRoom.getId())
                ));
    }

    public Slice<ChatRoomDto> readChatRooms(int page, int size) {

        valueOperations = redisTemplate.opsForValue();

        return chatRoomRepository.findAllByStatusOrderByCreatedAtDesc(ACTIVE,
                        PageRequest.of(page, size))
                .map(chatRoom -> ChatRoomDto.of(chatRoom,
                        getNumberOfMembers(chatRoom.getId())
                ));
    }

    public ChatRoomDto readChatRoom(Long chatroomId) {

        valueOperations = redisTemplate.opsForValue();

        return ChatRoomDto.of(
                chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                        .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND)),
                getNumberOfMembers(chatroomId)
        );
    }

    private Integer getNumberOfMembers(Long chatroomId) {
        return Optional.ofNullable(valueOperations.get(CHATROOM + chatroomId)).orElse(1);
    }

    public Slice<ChatDto> readChats(Long chatroomId, Member member, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));

        return chatRepository.findAllByChatRoomOrderByCreatedAtDesc(chatRoom,
                        PageRequest.of(page, size))
                .map(chat -> ChatDto.of(chat, Objects.equals(member.getId(), chat.getMember().getId())));
    }

    public ChatRoomStatusDto chatRoomsStatus() {
        HashOperations<String, String, ChatRoomSession> hashOperations = redisTemplate.opsForHash();
        return ChatRoomStatusDto.of(
                chatRoomRepository.countByStatus(ACTIVE),
                hashOperations.size(SESSION));
    }
}
