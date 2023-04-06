package zerobase.bud.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.repository.ChatRoomSessionRepository;

import java.util.Optional;

import static zerobase.bud.common.type.ErrorCode.CHATROOM_NOT_FOUND;
import static zerobase.bud.type.ChatRoomStatus.ACTIVE;

@RequiredArgsConstructor
@Component
public class WebSocketHandler implements ChannelInterceptor {

    private final ChatRoomSessionRepository chatRoomSessionRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            //TODO: member 부분 토큰 비교
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Long chatroomId = getChatroomIdFromDestination(accessor.getDestination());
            ChatRoom chatRoom = getChatRoom(chatroomId);
            chatRoomSessionRepository.save(ChatRoomSession.builder()
                    .sessionId(accessor.getSessionId())
                    .chatRoom(chatRoom)
                    .build());

        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Optional<ChatRoomSession> optionalSession =
                    chatRoomSessionRepository.findBySessionId(accessor.getSessionId());
            if (optionalSession.isPresent()) {
                ChatRoomSession session = optionalSession.get();
                session.setDelete();
                chatRoomSessionRepository.save(session);
            }
        }

        return message;
    }

    private Long getChatroomIdFromDestination(String destination) {
        return Long.parseLong(destination.substring(destination.lastIndexOf("/") + 1));
    }

    private ChatRoom getChatRoom(Long chatroomId) {
        return chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));
    }
}
