package zerobase.bud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import zerobase.bud.dto.ChatDto;
import zerobase.bud.exception.ChatException;
import zerobase.bud.type.ErrorCode;


@RequiredArgsConstructor
@Service
@Slf4j
public class RedisMessageSubscriber implements MessageListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String messageStr = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
        try {
            ChatDto chat = objectMapper.readValue(messageStr, ChatDto.class);
            log.error(message.getChannel().toString());
            messagingTemplate.convertAndSend(message.getChannel().toString(), chat);
        } catch (JsonProcessingException e) {
            throw new ChatException(ErrorCode.REDIS_BROKER_ERROR);
        }
    }
}
