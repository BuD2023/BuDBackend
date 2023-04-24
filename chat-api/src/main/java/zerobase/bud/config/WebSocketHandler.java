package zerobase.bud.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import zerobase.bud.domain.ChatRoom;
import zerobase.bud.domain.ChatRoomSession;
import zerobase.bud.dto.ChatDto;
import zerobase.bud.exception.ChatRoomException;
import zerobase.bud.exception.MemberException;
import zerobase.bud.repository.ChatRoomRepository;
import zerobase.bud.type.ChatType;
import zerobase.bud.type.ErrorCode;
import zerobase.bud.util.TokenProvider;

import static zerobase.bud.type.ChatRoomStatus.ACTIVE;
import static zerobase.bud.type.ErrorCode.CHATROOM_NOT_FOUND;
import static zerobase.bud.util.Constants.CHATROOM;
import static zerobase.bud.util.Constants.SESSION;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketHandler implements ChannelInterceptor {

    private static HashOperations<String, String, ChatRoomSession> hashOperations;

    private static ListOperations<String, String> listOperations;

    private final ChatRoomRepository chatRoomRepository;

    private final TokenProvider tokenProvider;

    private final RedisTemplate redisTemplate;

    private final ChannelTopic channelTopic;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String rawToken = accessor.getFirstNativeHeader("Authorization");
            log.error(rawToken);

            if (tokenProvider.validateRawToken(rawToken)) {
                throw new MemberException(ErrorCode.INVALID_TOKEN);
            }

            String userId = tokenProvider.getUserIdInRawToken(rawToken);
            log.error(userId);
            Long chatroomId = getChatroomIdFromDestination(accessor.getDestination());
            String sessionId = accessor.getSessionId();
            ChatRoom chatRoom = getChatRoom(chatroomId);

            addUser(chatroomId, userId);
            saveSession(chatRoom, userId, sessionId);
            notifyChatroomStatus(chatRoom.getId(), ChatType.ENTER);

        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            hashOperations = redisTemplate.opsForHash();
            assert sessionId != null;
            ChatRoomSession session = hashOperations.get(SESSION, sessionId);

            try {
                assert session != null;
                ChatRoom chatRoom = getChatRoom(session.getChatroomId());

                removeUser(chatRoom.getId(), session.getUserId());
                listOperations = redisTemplate.opsForList();
                notifyChatroomStatus(chatRoom.getId(), ChatType.EXIT);

                if (chatRoom.getMember().getUserId().equals(session.getUserId())) {
                    deleteChatroom(chatRoom);
                    notifyChatroomStatus(chatRoom.getId(), ChatType.EXPIRE);
                }

            } catch (ChatRoomException | NullPointerException | NumberFormatException e) {
                log.error("{}", e.getMessage());
            } finally {
                hashOperations.delete(SESSION, sessionId);
            }
        }

        return message;
    }

    private void deleteChatroom(ChatRoom chatRoom) {
        chatRoom.delete();
        chatRoomRepository.save(chatRoom);
    }

    private void saveSession(ChatRoom chatRoom, String userId, String sessionId) {
        hashOperations = redisTemplate.opsForHash();
        ChatRoomSession session = ChatRoomSession.builder()
                .chatroomId(chatRoom.getId())
                .userId(userId)
                .build();
        hashOperations.put(SESSION, sessionId, session);
    }

    private void addUser(Long chatroomId, String userId) {
        listOperations = redisTemplate.opsForList();
        listOperations.rightPush(CHATROOM + chatroomId, userId);
    }

    private void removeUser(Long chatroomId, String userId) {
        listOperations = redisTemplate.opsForList();
        listOperations.remove(CHATROOM + chatroomId, 0, userId);
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

    private void notifyChatroomStatus(Long chatroomId, ChatType chatType) {
        redisTemplate.convertAndSend(channelTopic.getTopic(),
                ChatDto.of(chatType, chatroomId, getNumberOfMembers(chatroomId)));
    }

    private Long getNumberOfMembers(Long chatroomId) {
        return listOperations.size(CHATROOM + chatroomId);
    }

}
