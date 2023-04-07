package zerobase.bud.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.util.Constant;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.repository.ChatRoomSessionRepository;
import zerobase.bud.security.TokenProvider;

import java.util.List;

import static zerobase.bud.common.type.ErrorCode.CHATROOM_NOT_FOUND;
import static zerobase.bud.type.ChatRoomStatus.ACTIVE;

@RequiredArgsConstructor
@Component
public class WebSocketHandler implements ChannelInterceptor {

    private final ChatRoomSessionRepository chatRoomSessionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TokenProvider tokenProvider;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            //TODO: member 부분 토큰 비교
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Long chatroomId = getChatroomIdFromDestination(accessor.getDestination());
            ChatRoom chatRoom = getChatRoom(chatroomId);

            String rawToken = (String) accessor.getHeader(Constant.TOKEN_HEADER);
            String userId = tokenProvider.parseRawToken(rawToken);

            chatRoomSessionRepository.save(ChatRoomSession.builder()
                    .sessionId(accessor.getSessionId())
                    .chatRoom(chatRoom)
                    .isOwner(chatRoom.getMember().getUserId().equals(userId))
                    .build());

        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            chatRoomSessionRepository.findBySessionId(accessor.getSessionId())
                    .ifPresent(chatRoomSession -> {
                        chatRoomSession.setDelete();

                        if (chatRoomSession.getIsOwner()) {
                            ChatRoom chatRoom = chatRoomSession.getChatRoom();
                            chatRoom.setDelete();
                            chatRoomRepository.save(chatRoom);
                            List<ChatRoomSession> sessions = chatRoomSessionRepository.findByChatRoom(chatRoom);
                            sessions.stream().forEach(session -> session.setDelete());
                        }

                        chatRoomSessionRepository.save(chatRoomSession);

                    });
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
