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
import zerobase.bud.chat.dto.ChatRoomDto;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.type.ChatRoomStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("채팅룸 생성 성공")
    void successCreateChatRoomTest() {
        //given
        given(chatRoomRepository.save(any())).willReturn(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .description("임의의 설명")
                        .hashTag("해시태그#해시태그2")
                        .status(ChatRoomStatus.ACTIVE)
                        .build());
        List<String> hashStr = Arrays.asList("해시태그1", "해시태그2");
        //when
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        Long result = chatRoomService
                .createChatRoom("챗지비티그거진짜어쩌구", "챗지비티그거진짜나쁘네", hashStr);
        //then
        verify(chatRoomRepository, times(1)).save(captor.capture());
        assertEquals("챗지비티그거진짜어쩌구", captor.getValue().getTitle());
        assertEquals("챗지비티그거진짜나쁘네", captor.getValue().getDescription());
        assertEquals("해시태그1#해시태그2", captor.getValue().getHashTag());
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
                .createChatRoom("챗지비티그거진짜어쩌구", "챗지비티그거진짜나쁘네", hashStr);
        //then
        verify(chatRoomRepository, times(1)).save(captor.capture());
        assertEquals("챗지비티그거진짜어쩌구", captor.getValue().getTitle());
        assertEquals("챗지비티그거진짜나쁘네", captor.getValue().getDescription());
        assertEquals("", captor.getValue().getHashTag());
        assertEquals(1L, result);
    }


    @Test
    @DisplayName("채팅룸 검색 성공")
    void successSearchChatRoomTest() {
        //given
        List<ChatRoom> chatRooms = Arrays.asList(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .numberOfMembers(1)
                        .description("임의의 첫번째 설명")
                        .hashTag("해시태그")
                        .status(ChatRoomStatus.ACTIVE)
                        .build(),

                ChatRoom.builder()
                        .id(2L)
                        .title("임의의두번째타이틀")
                        .numberOfMembers(5)
                        .description("임의의 설명")
                        .hashTag("해시태그#해시")
                        .status(ChatRoomStatus.ACTIVE)
                        .build(),

                ChatRoom.builder()
                        .id(3L)
                        .title("임의의세번째타이틀")
                        .numberOfMembers(6)
                        .description("임의의 설명")
                        .hashTag("해시태그#해시")
                        .status(ChatRoomStatus.ACTIVE)
                        .build()
        );

        given(chatRoomRepository
                .findAllByTitleContainingIgnoreCaseAndStatus(anyString(), any(), any()))
                .willReturn(new SliceImpl<>(chatRooms));
        //when
        Slice<ChatRoomDto> chatRoomDtos = chatRoomService.searchChatRoom("키워드", 0);
        //then
        assertEquals(3, chatRoomDtos.getContent().size());
        assertEquals(1L, chatRoomDtos.getContent().get(0).getChatRoomId());
        assertEquals("임의의타이틀", chatRoomDtos.getContent().get(0).getTitle());
        assertEquals(1, chatRoomDtos.getContent().get(0).getNumberOfMembers());
        assertEquals("임의의 첫번째 설명", chatRoomDtos.getContent().get(0).getDescription());
        assertEquals("해시태그", chatRoomDtos.getContent().get(0).getHashTags().get(0));
    }

    @Test
    @DisplayName("모든 채팅룸 검색 성공")
    void successGetChatRoomTest() {
        //given
        List<ChatRoom> chatRooms = Arrays.asList(
                ChatRoom.builder()
                        .id(1L)
                        .title("임의의타이틀")
                        .numberOfMembers(1)
                        .description("임의의 첫번째 설명")
                        .hashTag("해시태그#해시")
                        .status(ChatRoomStatus.ACTIVE)
                        .build(),
                ChatRoom.builder()
                        .id(2L)
                        .title("임의의두번째타이틀")
                        .description("임의의설명")
                        .hashTag("해시태그")
                        .numberOfMembers(5)
                        .status(ChatRoomStatus.ACTIVE)
                        .build(),
                ChatRoom.builder()
                        .id(3L)
                        .title("임의의세번째타이틀")
                        .description("임의의설명")
                        .hashTag("해시태그")
                        .numberOfMembers(6)
                        .status(ChatRoomStatus.ACTIVE)
                        .build()
        );

        given(chatRoomRepository
                .findAllByStatus(any(), any()))
                .willReturn(new SliceImpl<>(chatRooms));
        //when
        Slice<ChatRoomDto> chatRoomDtos = chatRoomService.getChatRoom(0);
        //then
        assertEquals(3, chatRoomDtos.getContent().size());
        assertEquals(2L, chatRoomDtos.getContent().get(1).getChatRoomId());
        assertEquals("임의의두번째타이틀", chatRoomDtos.getContent().get(1).getTitle());
        assertEquals(5, chatRoomDtos.getContent().get(1).getNumberOfMembers());
        assertEquals("임의의 첫번째 설명", chatRoomDtos.getContent().get(0).getDescription());
        assertEquals("해시태그", chatRoomDtos.getContent().get(0).getHashTags().get(0));
    }
}