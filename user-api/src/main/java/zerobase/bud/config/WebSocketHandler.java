package zerobase.bud.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import zerobase.bud.common.exception.ChatRoomException;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.repository.ChatRoomRepository;

import static zerobase.bud.common.type.ErrorCode.CHATROOM_NOT_FOUND;
import static zerobase.bud.common.util.Constants.CHATROOM;
import static zerobase.bud.common.util.Constants.SESSION;
import static zerobase.bud.type.ChatRoomStatus.ACTIVE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketHandler implements ChannelInterceptor {
    private final ChatRoomRepository chatRoomRepository;

    private final TokenProvider tokenProvider;

    private final RedisTemplate redisTemplate;

    private static HashOperations<String, String, ChatRoomSession> hashOperations;

    private static ValueOperations<String, Integer> valueOperations;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.error("preSend");

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String rawToken = accessor.getNativeHeader(HttpHeaders.AUTHORIZATION).get(0);

            if (tokenProvider.validateRawToken(rawToken)) {
                throw new MemberException(ErrorCode.INVALID_TOKEN);
            }

            String userId = tokenProvider.getUserIdInRawToken(rawToken);
            Long chatroomId = getChatroomIdFromDestination(accessor.getDestination());
            String sessionId = accessor.getSessionId();

            ChatRoom chatRoom = getChatRoom(chatroomId);

            hashOperations = redisTemplate.opsForHash();
            addSessionCount(chatroomId);
            saveSession(chatRoom, userId, sessionId);
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            hashOperations = redisTemplate.opsForHash();
            ChatRoomSession session = hashOperations.get(SESSION, sessionId);

            try {
                valueOperations = redisTemplate.opsForValue();

                ChatRoom chatRoom = getChatRoom(session.getChatroomId());
                minusSessionCount(chatRoom.getId());

                if (chatRoom.getMember().getUserId().equals(session.getUserId())) {
                    chatRoom.delete();
                    chatRoomRepository.save(chatRoom);
                    valueOperations.getAndDelete(CHATROOM + chatRoom.getId());
                }
            } catch (ChatRoomException | NullPointerException e) {
                log.error("{}", e.getMessage());
            } finally {
                hashOperations.delete(SESSION, sessionId);
            }

        }

        return message;
    }

    private void saveSession(ChatRoom chatRoom, String userId, String sessionId) {
        ChatRoomSession session = ChatRoomSession.builder()
                .chatroomId(chatRoom.getId())
                .userId(userId)
                .build();
        hashOperations.put(SESSION, sessionId, session);
    }

    private void addSessionCount(Long chatroomId) {
        valueOperations.setIfAbsent(CHATROOM + chatroomId, 0);
        valueOperations.increment(CHATROOM + chatroomId);
    }

    private void minusSessionCount(Long chatroomId) {
        valueOperations.decrement(CHATROOM + chatroomId);
    }

    private Long getChatroomIdFromDestination(String destination) {
        if (destination == null) {
            throw new ChatRoomException(ErrorCode.WEB_SOCKET_ERROR);
        }
        return Long.parseLong(destination.substring(destination.lastIndexOf("/") + 1));
    }

    private ChatRoom getChatRoom(Long chatroomId) {
        return chatRoomRepository.findByIdAndStatus(chatroomId, ACTIVE)
                .orElseThrow(() -> new ChatRoomException(CHATROOM_NOT_FOUND));
    }
}
