package ru.moskalev.demo.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.moskalev.demo.websockets.AggregationWebSocketHandler;

@Configuration
@EnableWebSocket
public class AggregationWebSocketConfig implements WebSocketConfigurer {

    private final AggregationWebSocketHandler aggregationWebSocketHandler;

    public AggregationWebSocketConfig(AggregationWebSocketHandler aggregationWebSocketHandler) {
        this.aggregationWebSocketHandler = aggregationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(aggregationWebSocketHandler, "/ws/clients-full-with-email")
                .setAllowedOrigins("*");
    }
}
