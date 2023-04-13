package zerobase.bud.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import zerobase.bud.chat.dto.ChatDto;
import zerobase.bud.common.exception.ChatException;
import zerobase.bud.common.type.ErrorCode;

@RequiredArgsConstructor
@Service
public class RedisMessageSubscriber implements MessageListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String messageStr = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
        try {
            ChatDto chat = objectMapper.readValue(messageStr, ChatDto.class);
            messagingTemplate.convertAndSend("/chatrooms/" + chat.getChatroomId(), chat);
        } catch (JsonProcessingException e) {
            throw new ChatException(ErrorCode.REDIS_BROKER_ERROR);
        }
    }
}
