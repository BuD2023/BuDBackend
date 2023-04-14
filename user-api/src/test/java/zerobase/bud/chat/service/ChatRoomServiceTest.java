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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.chat.dto.ChatRoomStatusDto;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Chat;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.ChatRepository;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.type.ChatRoomStatus;
import zerobase.bud.type.ChatType;
import zerobase.bud.type.MemberStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private ChatRepository chatRepository;

    @Mock
    private RedisTemplate<String, ?> redisTemplate;

    @Mock
    private ValueOperations valueOperations;

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
                .findByTitleContainsIgnoreCaseOrHashTagContainsIgnoreCaseAndStatus(anyString(), anyString(), any(), any()))
                .willReturn(new SliceImpl<>(chatRooms));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("CHATROOM1")).willReturn(2);
        given(valueOperations.get("CHATROOM2")).willReturn(3);
        given(valueOperations.get("CHATROOM3")).willReturn(3);
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
                .findAllByStatus(any(), any()))
                .willReturn(new SliceImpl<>(chatRooms));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("CHATROOM1")).willReturn(2);
        given(valueOperations.get("CHATROOM2")).willReturn(3);
        given(valueOperations.get("CHATROOM3")).willReturn(3);

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

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("CHATROOM123")).willReturn(2);
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
                        .member(member)
                        .message("이것은두번째메세지")
                        .type(ChatType.MESSAGE).build()
        );

        given(chatRoomRepository.findByIdAndStatus(anyLong(), any()))
                .willReturn(Optional.of(chatRoom));
        given(chatRepository.findAllByChatRoomOrderByCreatedAtDesc(any(), any()))
                .willReturn(new SliceImpl<>(chats));
        //when
        Slice<ChatDto> dtos = chatRoomService.readChats(12L, 1, 15);
        //then
        assertEquals(1L, dtos.getContent().get(0).getChatId());
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
                () -> chatRoomService.readChats(12L, 1, 10));
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
}