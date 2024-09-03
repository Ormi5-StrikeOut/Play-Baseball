package org.example.spring.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.message.messageDto.MessageResponseDto;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = String.valueOf(
                    redisTemplate
                            .getStringSerializer()
                            .deserialize(message.getBody()));

            MessageResponseDto dto = objectMapper.readValue(publishMessage, MessageResponseDto.class);
            log.info("message = {}", dto);
            messagingTemplate.convertAndSend("/sub/room/" + dto.getMessageRoomId(), dto);
            log.info("메세지 받음");
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

}
