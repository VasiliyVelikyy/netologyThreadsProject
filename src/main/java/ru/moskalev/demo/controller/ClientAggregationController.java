package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.domain.ClientFullInfo;
import ru.moskalev.demo.service.ClientAggregationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientAggregationController {
    private final ClientAggregationService aggregationService;

    @GetMapping("/clients-full")
    public ResponseEntity<List<ClientFullInfo>> getClientsFull() {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoAsync();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/clients-invoke-by-timeout")
    public ResponseEntity<List<ClientFullInfo>> getClientsFullWithInvokeByTimeout() throws InterruptedException {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoAsyncWithTimeout();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/clients-full-cancel")
    public ResponseEntity<List<ClientFullInfo>> getClientsFullWithWithCancelTask() {
        List<ClientFullInfo> result = aggregationService.getFullClientInfoAsyncWithCancel();
        return ResponseEntity.ok(result);
    }
}
