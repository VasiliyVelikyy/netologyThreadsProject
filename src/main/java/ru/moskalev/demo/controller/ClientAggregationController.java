package ru.moskalev.demo.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfo;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmail;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmailVerify;
import ru.moskalev.demo.service.ClientAggregationService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ClientAggregationController {
    private final ClientAggregationService aggregationService;
    private final Tracer tracer;

    @GetMapping("/clients-full")
    public ResponseEntity<List<ClientFullInfo>> getClientsFull() {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoWithFuture();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clients-full/with-virtual-threads")
    public ResponseEntity<List<ClientFullInfo>> getClientsFullWithVirtualThreads() {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoWithVirtualThreads();
        return ResponseEntity.ok(result);
    }


//    @GetMapping("/clients-full-with-email")
//    public ResponseEntity<List<ClientFullInfoWithEmail>> getClientsFullWithEmail() {
//        List<ClientFullInfoWithEmail> result = aggregationService.getFullClientInfoWithEmailAsyncWithLogs();
//        return ResponseEntity.ok(result);
//    }

    @GetMapping("/clients-full-with-email")
    public ResponseEntity<List<ClientFullInfoWithEmail>> getClientsFullWithEmail() {
        Span rootSpan = tracer.spanBuilder("GET /clients-full-with-email")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        try(var scope = rootSpan.makeCurrent()){
            List<ClientFullInfoWithEmail> result = aggregationService.getFullClientInfoWithEmailAsyncWithLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e ){
            rootSpan.recordException(e);
            rootSpan.setStatus(StatusCode.ERROR);
            throw e;
        }finally {
            rootSpan.end();
        }
    }

    @GetMapping("/clients-full-with-email/with-virtual-threads")
    public ResponseEntity<List<ClientFullInfoWithEmail>> getClientsFullWithEmailWithVirtualThreads() throws Exception {
        List<ClientFullInfoWithEmail> result = aggregationService.getFullClientInfoWithEmailVT();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clients-full-with-email/verify")
    public ResponseEntity<List<ClientFullInfoWithEmailVerify>> getClientsFullWithEmailVerify() {
        List<ClientFullInfoWithEmailVerify> result = aggregationService.getFullClientInfoWithEmailVerifyAsync();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clients-invoke-by-timeout")
    public ResponseEntity<List<ClientFullInfo>> getClientsFullWithInvokeByTimeout() throws InterruptedException {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoAsyncWithTimeout();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clients-full-cancel")
    public ResponseEntity<List<ClientFullInfo>> getClientsFullWithWithCancelTask() {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoAsyncWithCancel();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clients-info-with-timeout")
    public ResponseEntity<List<ClientFullInfoWithEmail>> getClientsFullWithEmailWithTimeout() {
        List<ClientFullInfoWithEmail> result = aggregationService.getClientsFullWithEmailWithTimeout();
        return ResponseEntity.ok(result);
    }
}
