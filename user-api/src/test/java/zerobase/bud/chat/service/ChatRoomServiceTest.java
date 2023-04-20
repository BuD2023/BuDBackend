package zerobase.bud.chat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.chat.dto.ChatRoomStatusDto;
import zerobase.bud.chat.dto.ChatUserDto;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Chat;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.type.ChatRoomStatus;
import zerobase.bud.type.ChatType;
import zerobase.bud.type.MemberStatus;
import zerobase.bud.user.repository.FollowRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private RedisTemplate<String, ?> redisTemplate;

    @Mock
    private ListOperations listOperations;

    @Mock
    private HashOperations hashOperations;

    @InjectMocks
    private ChatRoomService chatRoomService;

    Member member = Member.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .status(MemberStatus.VERIFIED)
            .profileImg("abcde.jpg")
            .nickname("안뇽")
            .job("시스템프로그래머")
            .oAuthAccessToken("tokenvalue")
            .build();

    @Test
    @DisplayName("채팅룸 생성 성공")
    void successCreateChatRoomTest() {
        //given
        given(chatRoomRepository.save(any())).willReturn(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .description("임의의 설명")
                        .hashTag("#해시#태크#")
                        .status(ChatRoomStatus.ACTIVE)
                        .build());
        List<String> hashStr = Arrays.asList("해시태그1", "해시태그2");
        //when
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        Long result = chatRoomService
                .createChatRoom("챗지비티그거진짜어쩌구", "챗지비티그거진짜나쁘네", hashStr, member);
        //then
        verify(chatRoomRepository, times(1)).save(captor.capture());
        assertEquals("챗지비티그거진짜어쩌구", captor.getValue().getTitle());
        assertEquals("챗지비티그거진짜나쁘네", captor.getValue().getDescription());
        assertEquals("#해시태그1#해시태그2#", captor.getValue().getHashTag());
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("채팅룸 생성 성공 - 해시 태그 없을 때")
    void successCreateChatRoomWhenHashtagIsEmptyTest() {
        //given
        given(chatRoomRepository.save(any())).willReturn(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .description("임의의 설명")
                        .hashTag("")
                        .status(ChatRoomStatus.ACTIVE)
                        .build());
        List<String> hashStr = new ArrayList<>();
        //when
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        Long result = chatRoomService
                .createChatRoom("챗지비티그거진짜어쩌구", "챗지비티그거진짜나쁘네", hashStr, member);
        //then
        verify(chatRoomRepository, times(1)).save(captor.capture());
        assertEquals("챗지비티그거진짜어쩌구", captor.getValue().getTitle());
        assertEquals("챗지비티그거진짜나쁘네", captor.getValue().getDescription());
        assertEquals("", captor.getValue().getHashTag());
        assertEquals(1L, result);
    }


    @Test
    @DisplayName("채팅룸 검색 성공")
    void successSearchChatRoomsTest() {
        //given
        List<ChatRoom> chatRooms = Arrays.asList(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .description("임의의 첫번째 설명")
                        .createdAt(LocalDateTime.now())
                        .hashTag("해시태그")
                        .member(member)
                        .status(ChatRoomStatus.ACTIVE)
                        .build(),

                ChatRoom.builder()
                        .id(2L)
                        .title("임의의두번째타이틀")
                        .description("임의의 설명")
                        .createdAt(LocalDateTime.now())
                        .hashTag("해시태그#해시")
                        .member(member)
                        .status(ChatRoomStatus.ACTIVE)
                        .build(),

                ChatRoom.builder()
                        .id(3L)
                        .title("임의의세번째타이틀")
                        .description("임의의 설명")
                        .createdAt(LocalDateTime.now())
                        .hashTag("해시태그#해시")
                        .member(member)
                        .status(ChatRoomStatus.ACTIVE)
                        .build()
        );

        given(chatRoomRepository
                .findByTitleContainsIgnoreCaseOrHashTagContainsIgnoreCaseAndStatusOrderByCreatedAtDesc(anyString(), anyString(), any(), any()))
                .willReturn(new SliceImpl<>(chatRooms));

        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.size("CHATROOM1")).willReturn(2L);
        given(listOperations.size("CHATROOM2")).willReturn(3L);
        given(listOperations.size("CHATROOM3")).willReturn(3L);
        //when
        Slice<ChatRoomDto> chatRoomDtos = chatRoomService.searchChatRooms("키워드", 0, 3);
        //then
        assertEquals(3, chatRoomDtos.getContent().size());
        assertEquals(1L, chatRoomDtos.getContent().get(0).getChatRoomId());
        assertEquals("임의의타이틀", chatRoomDtos.getContent().get(0).getTitle());
        assertEquals("임의의 첫번째 설명", chatRoomDtos.getContent().get(0).getDescription());
        assertEquals("해시태그", chatRoomDtos.getContent().get(0).getHashTags().get(0));
        assertEquals("안뇽", chatRoomDtos.getContent().get(0).getHostName());
        assertEquals(1L, chatRoomDtos.getContent().get(0).getHostId());
    }

    @Test
    @DisplayName("모든 채팅룸 검색 성공")
    void successReadChatRoomsTest() {
        //given
        List<ChatRoom> chatRooms = Arrays.asList(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .description("임의의 첫번째 설명")
                        .hashTag("해시태그#해시")
                        .createdAt(LocalDateTime.now())
                        .status(ChatRoomStatus.ACTIVE)
                        .member(member)
                        .build(),
                ChatRoom.builder()
                        .id(2L)
                        .title("임의의두번째타이틀")
                        .description("임의의설명")
                        .hashTag("해시태그")
                        .createdAt(LocalDateTime.now())
                        .status(ChatRoomStatus.ACTIVE)
                        .member(member)
                        .build(),
                ChatRoom.builder()
                        .id(3L)
                        .title("임의의세번째타이틀")
                        .description("임의의설명")
                        .hashTag("해시태그")
                        .createdAt(LocalDateTime.now())
                        .status(ChatRoomStatus.ACTIVE)
                        .member(member)
                        .build()
        );

        given(chatRoomRepository
                .findAllByStatusOrderByCreatedAtDesc(any(), any()))
                .willReturn(new SliceImpl<>(chatRooms));

        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.size("CHATROOM1")).willReturn(2L);
        given(listOperations.size("CHATROOM2")).willReturn(3L);
        given(listOperations.size("CHATROOM3")).willReturn(3L);

        //when
        Slice<ChatRoomDto> chatRoomDtos = chatRoomService.readChatRooms(0, 5);
        //then
        assertEquals(3, chatRoomDtos.getContent().size());
        assertEquals(2L, chatRoomDtos.getContent().get(1).getChatRoomId());
        assertEquals("임의의두번째타이틀", chatRoomDtos.getContent().get(1).getTitle());
        assertEquals("임의의 첫번째 설명", chatRoomDtos.getContent().get(0).getDescription());
        assertEquals("해시태그", chatRoomDtos.getContent().get(0).getHashTags().get(0));
        assertEquals("안뇽", chatRoomDtos.getContent().get(0).getHostName());
        assertEquals(1L, chatRoomDtos.getContent().get(0).getHostId());
    }

    @Test
    @DisplayName("채팅룸 정보 가져오기 성공")
    void successReadChatRoomTest() {
        //given
        given(chatRoomRepository.findByIdAndStatus(any(), any()))
                .willReturn(
                        Optional.of(ChatRoom.builder()
                                .id(1L)
                                .title("임의의타이틀")
                                .description("임의의 첫번째 설명")
                                .hashTag("#해시태그#해시#")
                                .status(ChatRoomStatus.ACTIVE)
                                .member(member)
                                .createdAt(LocalDateTime.now())
                                .build())
                );

        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.size("CHATROOM123")).willReturn(2L);
        //when
        ChatRoomDto dto = chatRoomService.readChatRoom(123L);
        //then
        assertEquals(1L, dto.getChatRoomId());
        assertEquals("임의의타이틀", dto.getTitle());
        assertEquals("임의의 첫번째 설명", dto.getDescription());
        assertEquals("해시태그", dto.getHashTags().get(0));
        assertEquals("안뇽", dto.getHostName());
    }

    @Test
    @DisplayName("채팅룸 내에서 채팅 리스트 가져오기 성공")
    void successReadChatsTest() {
        //given
        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .title("임의의타이틀")
                .description("임의의 첫번째 설명")
                .hashTag("해시태그#해시")
                .status(ChatRoomStatus.ACTIVE)
                .build();

        Member chatUser = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .build();

        List<Chat> chats = Arrays.asList(
                Chat.builder()
                        .chatRoom(chatRoom)
                        .id(1L)
                        .createdAt(LocalDateTime.now())
                        .message("이것은메세지")
                        .member(member)
                        .type(ChatType.MESSAGE).build(),
                Chat.builder()
                        .chatRoom(chatRoom)
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(chatUser)
                        .message("이것은두번째메세지")
                        .type(ChatType.MESSAGE).build()
        );

        given(chatRoomRepository.findByIdAndStatus(anyLong(), any()))
                .willReturn(Optional.of(chatRoom));
        given(chatRepository.findAllByChatRoomOrderByCreatedAtDesc(any(), any()))
                .willReturn(new SliceImpl<>(chats));
        //when
        Slice<ChatDto> dtos = chatRoomService.readChats(12L, member, 1, 15);
        //then
        assertEquals(1L, dtos.getContent().get(0).getChatId());
        assertEquals(false, dtos.getContent().get(1).getIsReader());
        assertEquals(true, dtos.getContent().get(0).getIsReader());
        assertEquals("이것은메세지", dtos.getContent().get(0).getMessage());
        assertEquals(ChatType.MESSAGE, dtos.getContent().get(0).getChatType());
        assertEquals(1L, dtos.getContent().get(0).getUserId());
        assertEquals("안뇽", dtos.getContent().get(0).getUserName());
    }

    @Test
    @DisplayName("채팅룸 내에서 채팅 리스트 가져오기 실패 - 채팅방 없음")
    void failReadChatsTest() {
        //given
        given(chatRoomRepository.findByIdAndStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatRoomService.readChats(12L, member, 1, 10));
        //then
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 현황 읽기 성공")
    void successChatRoomsStatusTest() {
        //given
        given(chatRoomRepository.countByStatus(ChatRoomStatus.ACTIVE))
                .willReturn(3L);

        given(redisTemplate.opsForHash()).willReturn(hashOperations);
        given(hashOperations.size(anyString())).willReturn(24L);
        //when
        ChatRoomStatusDto dto = chatRoomService.chatRoomsStatus();
        //then
        assertEquals(3L, dto.getNumberOfChatRooms());
        assertEquals(24L, dto.getNumberOfUsers());

    }

    @Test
    @DisplayName("호스트 변경 성공")
    void successModifyHostTest() {
        //given
        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .title("임의의타이틀")
                .description("임의의 첫번째 설명")
                .hashTag("#해시태그#해시#")
                .status(ChatRoomStatus.ACTIVE)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        Member newHost = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .build();

        given(chatRoomRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(chatRoom));
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(newHost));
        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.range("CHATROOM1", 0, -1)).willReturn(List.of(1L, 2L, 3L));
        //when
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        Long result = chatRoomService.modifyHost(1L, 2L, member);
        //then
        verify(chatRoomRepository, times(1)).save(captor.capture());
        assertEquals(newHost, captor.getValue().getMember());
        assertEquals(result, 2L);
    }

    @Test
    @DisplayName("호스트 변경 실패 - 채팅방 없음")
    void failModifyHostTest_ChatRoomNotFound() {
        //given
        given(chatRoomRepository.findByIdAndStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatRoomService.modifyHost(12L, 1L, member));
        //then
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("호스트 변경 실패 - 요청한 유저가 호스트가 아님")
    void failModifyHostTest_NotChatRoomUsersRequest() {
        //given
        Member host = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .build();

        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .title("임의의타이틀")
                .description("임의의 첫번째 설명")
                .hashTag("#해시태그#해시#")
                .status(ChatRoomStatus.ACTIVE)
                .member(host)
                .createdAt(LocalDateTime.now())
                .build();

        given(chatRoomRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(chatRoom));
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatRoomService.modifyHost(12L, 1L, member));
        //then
        assertEquals(ErrorCode.NOT_CHATROOM_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("호스트 변경 실패 - 해당 유저 찾을 수 없음")
    void failModifyHostTest_MemberNotFound() {
        //given
        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .title("임의의타이틀")
                .description("임의의 첫번째 설명")
                .hashTag("#해시태그#해시#")
                .status(ChatRoomStatus.ACTIVE)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        given(chatRoomRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(chatRoom));
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> chatRoomService.modifyHost(12L, 1L, member));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }

    @Test
    @DisplayName("호스트 변경 실패 - 위임할 멤버가 채팅방에 참여중이지 않음")
    void failModifyHostTest_MemberNotFoundInChatRoom() {
        //given
        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .title("임의의타이틀")
                .description("임의의 첫번째 설명")
                .hashTag("#해시태그#해시#")
                .status(ChatRoomStatus.ACTIVE)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        Member newHost = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .build();

        given(chatRoomRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(chatRoom));
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(newHost));
        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.range("CHATROOM12", 0, -1)).willReturn(List.of(2L, 3L));
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatRoomService.modifyHost(12L, 1L, member));
        //then
        assertEquals(ErrorCode.MEMBER_NOT_FOUND_IN_CHATROOM, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅에 참여중인 멤버 리스트 불러오기 성공")
    void successReadChatUsersTest() {
        //given
        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .title("임의의타이틀")
                .description("임의의 첫번째 설명")
                .hashTag("#해시태그#해시#")
                .status(ChatRoomStatus.ACTIVE)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        Member chatUser1 = Member.builder()
                .id(2L)
                .userId("thefn")
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세요 저는 어쩌구저쩌구")
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .build();

        Member chatUser2 = Member.builder()
                .id(3L)
                .userId("trowds")
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세용")
                .profileImg("ddddd.jpg")
                .nickname("닉넴고갈")
                .build();

        given(chatRoomRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(chatRoom));
        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.range("CHATROOM12", 0, -1)).willReturn(List.of(2L, 3L));
        given(memberRepository.findAllByIdIn(anyList()))
                .willReturn(Stream.of(member, chatUser1, chatUser2));
        given(followRepository.existsByTargetAndMember(member, member)).willReturn(false);
        given(followRepository.existsByTargetAndMember(chatUser1, member)).willReturn(true);
        given(followRepository.existsByTargetAndMember(chatUser2, member)).willReturn(false);
        //when
        List<ChatUserDto> dtos = chatRoomService.readChatUsers(12L, member);
        //then
        assertEquals(true, dtos.get(0).getIsReader());
        assertEquals(3, dtos.size());
        assertEquals(true, dtos.get(1).getIsFollowing());
        assertEquals(false, dtos.get(2).getIsFollowing());
    }

    @Test
    @DisplayName("채팅에 참여중인 멤버 리스트 불러오기 성공 - 채팅방 없음")
    void failReadChatUsersTest_ChatRoomNotFound() {
        //given
        given(chatRoomRepository.findByIdAndStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatRoomService.readChatUsers(12L, member));
        //then
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
    }
}