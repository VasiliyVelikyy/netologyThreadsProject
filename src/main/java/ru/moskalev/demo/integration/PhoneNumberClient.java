package ru.moskalev.demo.integration;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static ru.moskalev.demo.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneNumberClient {
    private final Tracer tracer;
    private final WebClient webClient;
    private final RestTemplate restTemplate;

    public CompletableFuture<String> getPhoneNumberAsync(String accountNumber) {
        String url = LOCAL_HOST + URL_PHONE_BY_GOOD_ACCOUNT;

        Span clientSpan = tracer.spanBuilder("GET "+URL_PHONE_BY_BAD_ACCOUNT)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("account.number", accountNumber)
                .startSpan();

        try (var scope = clientSpan.makeCurrent()) {
            return webClient.get()
                    .uri(url, accountNumber)
                    .retrieve()
                    .onStatus(httpStatusCode -> !httpStatusCode.is2xxSuccessful(),
                            response -> {
                                clientSpan.setStatus(StatusCode.ERROR, "HTTP " + response.statusCode());
                                return Mono.error(new RuntimeException("Non-2xx response " + response.statusCode()));
                            }
                    )
                    .bodyToMono(String.class)
                    .doOnSuccess(phone -> {
                        clientSpan.setStatus(StatusCode.OK);
                    })
                    .doOnError(throwable -> {
                        clientSpan.recordException(throwable);
                        clientSpan.setStatus(StatusCode.ERROR, throwable.getMessage());
                    })
                    .doFinally(elem -> {
                                clientSpan.end();
                            }
                    )
                    .toFuture();
        }

    }
}
