package ru.moskalev.demo.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.balance.BalanceMonitor;

@RestController
@RequiredArgsConstructor
public class HelloEndpoint {
    private final Tracer tracer;
    private final BalanceMonitor balanceMonitor;

    @GetMapping("/api/hello")
    public String hello() {
        Span span = tracer.spanBuilder("hello-Logic").startSpan();
        try (var scope = span.makeCurrent()) {
            try {
                Thread.sleep(100);
                balanceMonitor.checkForHighBalance();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Hello form trace method";
        } finally {
            span.end();
        }
    }
}
