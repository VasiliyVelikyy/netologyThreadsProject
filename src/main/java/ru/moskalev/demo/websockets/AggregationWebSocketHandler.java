package ru.moskalev.demo.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.moskalev.demo.service.aggrigation.ClientAggregationService;

@Component
@Slf4j
public class AggregationWebSocketHandler extends TextWebSocketHandler {
    private final ClientAggregationService clientAggregationService;
    private final ObjectMapper objectMapper;

    public AggregationWebSocketHandler(ClientAggregationService clientAggregationService, ObjectMapper objectMapper) {
        this.clientAggregationService = clientAggregationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            log.info("request websocket");
            var results = clientAggregationService.getFullClientInfoWithEmailAsync();
            String json = objectMapper.writeValueAsString(results);
            session.sendMessage(new TextMessage(json));
            session.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("websocket connection closed={}", status);
        super.afterConnectionClosed(session, status);
    }
}
