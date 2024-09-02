package org.example.spring.config.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constant.ErrorCode;
import org.example.spring.exception.MessageException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketHandler implements ChannelInterceptor {
    private final JwtTokenizer jwtTokenizer;

    /* 웹소켓 필터 역할 */
    @Override
    @CrossOrigin
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.ABORT.CONNECT == accessor.getCommand()) {
            String jwt = Optional.of(accessor.getFirstNativeHeader("Authorization")
                            .substring("Bearer ".length()))
                    .orElseThrow(() -> new MessageException(ErrorCode.UNAUTHORIZED_MESSAGE_ACCESS));
            jwtTokenizer.verifyAccessToken(jwt);
        }
        return message;
    }
}
