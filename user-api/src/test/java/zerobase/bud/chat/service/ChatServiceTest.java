package zerobase.bud.chat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.awss3.AwsS3Api;
import zerobase.bud.chat.dto.ChatDto;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AwsS3Api awsS3Api;

    @InjectMocks
    private ChatService chatService;

    Member member = Member.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .status(MemberStatus.VERIFIED)
            .profileImg("abcde.jpg")
            .nickname("안뇽")
            .job("시스템프로그래머")
            .oAuthAccessToken("tokenvalue")
            .build();

    ChatRoom chatRoom = ChatRoom.builder()
            .id(1L)
            .title("임의의타이틀")
            .description("임의의 첫번째 설명")
            .hashTag("해시태그#해시")
            .status(ChatRoomStatus.ACTIVE)
            .member(member)
            .createdAt(LocalDateTime.now())
            .build();

//    @Test
//    @DisplayName("채팅 전송 성공")
//    void successChattingTest() {
//        //given
//        given(chatRoomRepository.findByIdAndStatus(any(), any()))
//                .willReturn(Optional.of(chatRoom));
//
//        given(memberRepository.findById(anyLong()))
//                .willReturn(Optional.of(member));
//
//        given(chatRepository.save(any()))
//                .willReturn(Chat.builder()
//                        .chatRoom(chatRoom)
//                        .id(1L)
//                        .createdAt(LocalDateTime.now())
//                        .message("어쩌구저쩌구")
//                        .member(member)
//                        .type(ChatType.MESSAGE).build()
//                );
//
//        //when
//        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
//        chatService.chatting("어떤메시지", 1L, 2L);
//        //then
//        verify(chatRepository, times(1)).save(captor.capture());
//        assertEquals("어떤메시지", captor.getValue().getMessage());
//        assertEquals(1L, captor.getValue().getChatRoom().getId());
//        assertEquals(1L, captor.getValue().getMember().getId());
//        assertEquals(ChatType.MESSAGE, captor.getValue().getType());
//        assertEquals(1L, chatDto.getChatId());
//        assertEquals("어쩌구저쩌구", chatDto.getMessage());
//        assertTrue(chatDto.getCreatedAt().contains("초 전"));
//    }

    @Test
    @DisplayName("채팅 전송 실패 - 채팅방 없음")
    void failChattingWhenChatRoomNotFoundTest() {
        //given
        given(chatRoomRepository.findByIdAndStatus(any(), any()))
                .willReturn(Optional.empty());
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatService.chatting("임의의메세지", 1L, 2L));
        //then
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅 전송 실패 - 유저 없음")
    void failChattingWhenMemberNotFoundTest() {
        //given
        given(chatRoomRepository.findByIdAndStatus(any(), any()))
                .willReturn(Optional.of(chatRoom));

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> chatService.chatting("임의의메세지", 1L, 2L));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }

//    @Test
//    @DisplayName("이미지 전송 성공")
//    void successImageTest() {
//        //given
//        given(chatRoomRepository.findByIdAndStatus(any(), any()))
//                .willReturn(Optional.of(chatRoom));
//
//        given(memberRepository.findById(anyLong()))
//                .willReturn(Optional.of(member));
//
//        given(chatRepository.save(any()))
//                .willReturn(Chat.builder()
//                        .chatRoom(chatRoom)
//                        .id(1L)
//                        .createdAt(LocalDateTime.now())
//                        .message("filepath.jpg")
//                        .member(member)
//                        .type(ChatType.IMAGE).build()
//                );
//
//        given(awsS3Api.uploadFileImage(any(), any())).willReturn("image.jpg");
//
//        //when
//        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
//        chatService.image("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wCEABAMBg8GBREPDg",
//                1L, 2L);
//        //then
//        verify(chatRepository, times(1)).save(captor.capture());
//        assertEquals(1L, captor.getValue().getChatRoom().getId());
//        assertEquals(1L, captor.getValue().getMember().getId());
//        assertEquals("image.jpg", captor.getValue().getMessage());
//        assertEquals(ChatType.IMAGE, captor.getValue().getType());
//        assertEquals(1L, chatDto.getChatId());
//        assertEquals("filepath.jpg", chatDto.getMessage());
//        assertTrue(chatDto.getCreatedAt().contains("초 전"));
//    }

    @Test
    @DisplayName("이미지 전송 실패 - 채팅방 없음")
    void failImageWhenChatRoomNotFoundTest() {
        //given
        given(chatRoomRepository.findByIdAndStatus(any(), any()))
                .willReturn(Optional.empty());
        //when
        ChatRoomException exception = assertThrows(ChatRoomException.class,
                () -> chatService.chatting("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wCEABAMBg8GBREPDg",
                        1L, 2L));
        //then
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미지 전송 실패 - 유저 없음")
    void failImageWhenMemberNotFoundTest() {
        //given
        given(chatRoomRepository.findByIdAndStatus(any(), any()))
                .willReturn(Optional.of(chatRoom));

        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> chatService.chatting("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wCEABAMBg8GBREPDg",
                        1L, 2L));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }

}