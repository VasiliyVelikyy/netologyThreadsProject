package ru.moskalev.demo.integration.client;

import io.grpc.Deadline;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.moskalev.demo.AccountServiceGrpc;
import ru.moskalev.demo.GetContactInfoRequest;
import ru.moskalev.demo.GetContactInfoResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static ru.moskalev.demo.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneNumberClient {
    public static final String PHONE_NUMBER_URL = LOCAL_HOST + URL_PHONE_BY_GOOD_ACCOUNT;
    private final Tracer tracer;
    private final WebClient webClient;
    private final RestTemplate restTemplate;

    private final AccountServiceGrpc.AccountServiceFutureStub grpcClient;

    public CompletableFuture<String> getPhoneNumberAsyncWithTelemetry(String accountNumber) {

        Span clientSpan = tracer.spanBuilder("GET " + URL_PHONE_BY_BAD_ACCOUNT)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("account.number", accountNumber)
                .startSpan();

        try (var scope = clientSpan.makeCurrent()) {
            return webClient.get()
                    .uri(PHONE_NUMBER_URL, accountNumber)
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

    public CompletableFuture<String> getPhoneNumberAsync(String accountNumber) {
        return webClient.get()
                .uri(PHONE_NUMBER_URL, accountNumber)
                .retrieve()
                .onStatus(httpStatusCode -> !httpStatusCode.is2xxSuccessful(),
                        response -> Mono.error(new RuntimeException("Non-2xx response " + response.statusCode()))
                )
                .bodyToMono(String.class)
                .toFuture();
    }

    public CompletableFuture<String> getPhoneNumberAsyncGrpc(String accNumber) {
        GetContactInfoRequest request = GetContactInfoRequest
                .newBuilder()
                .setAccountNumber(accNumber)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return connectToGrpc(request);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private String connectToGrpc(GetContactInfoRequest request) throws InterruptedException, ExecutionException {
        GetContactInfoResponse response = grpcClient
                .withDeadline(Deadline.after(1, TimeUnit.SECONDS))
                .getContactInfo(request)
                .get();
        return response.getPhone();
    }

    public String getPhoneNumBlockedRest(String accNum) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(PHONE_NUMBER_URL, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("NON200");
            }
            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPhoneNumGrpc(String accNum) {
        GetContactInfoRequest request = GetContactInfoRequest
                .newBuilder()
                .setAccountNumber(accNum)
                .build();

        try {
            return connectToGrpc(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
