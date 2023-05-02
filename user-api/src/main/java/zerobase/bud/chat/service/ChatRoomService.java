package zerobase.bud.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.chat.dto.ChatRoomStatusDto;
import zerobase.bud.chat.dto.ChatUserDto;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.user.repository.FollowRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static zerobase.bud.common.type.ErrorCode.*;
import static zerobase.bud.type.ChatRoomStatus.ACTIVE;
import static zerobase.bud.util.Constants.CHATROOM;
import static zerobase.bud.util.Constants.SESSION;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final MemberRepository memberRepository;

    private final ChatRepository chatRepository;

    private final FollowRepository followRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static SetOperations<String, String> setOperations;

    public Long createChatRoom(String title, String description, List<String> hashTag, Member member) {
        String hashStr = "";
        if (!hashTag.isEmpty()) {
            hashStr = "#" + String.join("#", hashTag) + "#";
        }

        return chatRoomRepository.save(
                ChatRoom.builder()
                        .title(title)
                        .status(ACTIVE)
                        .hashTag(hashStr)
                        .description(description)
                        .member(member)
                        .build()).getId();
    }

    public Slice<ChatRoomDto> searchChatRooms(String keyword, int page, int size) {
        setOperations = redisTemplate.opsForSet();

        return chatRoomRepository
                .findByTitleContainsIgnoreCaseOrHashTagContainsIgnoreCaseAndStatusOrderByCreatedAtDesc(
                        keyword, "#" + keyword + "#", ACTIVE, PageRequest.of(page, size))
                .map(chatRoom -> ChatRoomDto.of(chatRoom,
                        getNumberOfMembers(chatRoom.getId())
                ));
    }

    public Slice<ChatRoomDto> readChatRooms(int page, int size) {
        setOperations = redisTemplate.opsForSet();

        return chatRoomRepository.findAllByStatusOrderByCreatedAtDesc(ACTIVE,
                        PageRequest.of(page, size))
                .map(chatRoom -> ChatRoomDto.of(chatRoom,
                        getNumberOfMembers(chatRoom.getId())
                ));
    }

    public ChatRoomDto readChatRoom(Long chatroomId) {
        setOperations = redisTemplate.opsForSet();

        return ChatRoomDto.of(
                chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                        .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND)),
                getNumberOfMembers(chatroomId)
        );
    }

    private Long getNumberOfMembers(Long chatroomId) {
        return setOperations.size(CHATROOM + chatroomId);
    }

    public Slice<ChatDto> readChats(Long chatroomId, Member member, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));

        return chatRepository.findAllByChatRoomOrderByCreatedAtDesc(chatRoom,
                        PageRequest.of(page, size))
                .map(chat -> ChatDto.of(chat,
                        Objects.equals(member.getId(), chat.getMember().getId())));
    }

    public ChatRoomStatusDto chatRoomsStatus() {
        HashOperations<String, String, ChatRoomSession> hashOperations = redisTemplate.opsForHash();
        long numberOfChatRoom = chatRoomRepository.countByStatus(ACTIVE);
        return ChatRoomStatusDto.of(
                numberOfChatRoom,
                numberOfChatRoom == 0 ? 0 : hashOperations.size(SESSION));
    }


    public Long modifyHost(Long chatroomId, Long userId, Member member) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));

        if (!Objects.equals(member.getId(), chatRoom.getMember().getId())) {
            throw new ChatRoomException(NOT_CHATROOM_OWNER);
        }

        Member newHost = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        if (!getUserList(chatroomId).contains(newHost.getUserId())) {
            throw new ChatRoomException(MEMBER_NOT_FOUND_IN_CHATROOM);
        }

        chatRoom.modifyHost(newHost);
        chatRoomRepository.save(chatRoom);
        return userId;
    }

    @Transactional(readOnly = true)
    public List<ChatUserDto> readChatUsers(Long chatroomId, Member member) {
        chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));

        return memberRepository.findAllByUserIdIn(getUserList(chatroomId)).stream()
                .map(chatUser -> ChatUserDto.of(
                        chatUser,
                        Objects.equals(chatUser.getId(), member.getId()),
                        followRepository.existsByTargetAndMember(chatUser, member)))
                .collect(Collectors.toList());
    }

    private Set<String> getUserList(Long chatroomId) {
        setOperations = redisTemplate.opsForSet();
        return setOperations.members(CHATROOM + chatroomId);
    }

    @Transactional
    public ChatUserDto chatUserProfile(Long chatroomId, Long userId) {
        chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_REGISTERED_MEMBER));

        if (!getUserList(chatroomId).contains(member.getUserId())) {
            throw new ChatRoomException(MEMBER_NOT_FOUND_IN_CHATROOM);
        }

        return ChatUserDto.of(member, true);
    }
}
