package ru.moskalev.demo.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.moskalev.demo.domain.ClientBalanceDto;
import ru.moskalev.demo.service.balance.ClientBalanceService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiveBalanceWebSocketHandler extends TextWebSocketHandler {

    private final ClientBalanceService clientBalanceServe;
    private final ObjectMapper objectMapper;

    private Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sendFullClientsBalances(session);
    }

    private void sendFullClientsBalances(WebSocketSession session) {
        log.info("request to websocket");
        try {
            var fullList = clientBalanceServe.getClientBalances();
            if (sessions.isEmpty()) return;
            String json = objectMapper.writeValueAsString(fullList);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void sendUpdate(ClientBalanceDto update) {
        try {
            if (sessions.isEmpty()) return;
            String json = objectMapper.writeValueAsString(update);

            sessions.forEach(session->{
                if(session.isOpen()){
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}
