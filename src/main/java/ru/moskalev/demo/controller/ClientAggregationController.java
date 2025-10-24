package ru.moskalev.demo.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ClientAggregationController {
    private final ClientAggregationService aggregationService;

    @GetMapping("/clients-full")
    public ResponseEntity<List<ClientFullInfo>> getClientsFull() {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoWithFuture();
        return ResponseEntity.ok(result);
    }


    @GetMapping("/clients-full-with-email")
    public ResponseEntity<List<ClientFullInfoWithEmail>> getClientsFullWithEmail() {
        List<ClientFullInfoWithEmail> result = aggregationService.getFullClientInfoWithEmailAsyncWithLogs();
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
