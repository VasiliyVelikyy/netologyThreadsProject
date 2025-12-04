package ru.moskalev.demo.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.moskalev.demo.websockets.LiveBalanceWebSocketHandler;

@Configuration
@EnableWebSocket
public class LiveBalanceWebSocketConfig implements WebSocketConfigurer {
    private final LiveBalanceWebSocketHandler liveBalanceWebSocketHandler;

    public LiveBalanceWebSocketConfig(LiveBalanceWebSocketHandler liveBalanceWebSocketHandler) {
        this.liveBalanceWebSocketHandler = liveBalanceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveBalanceWebSocketHandler, "/ws/clients-balance")
                .setAllowedOrigins("*");
    }
}
