package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.domain.ClientBalanceDto;
import ru.moskalev.demo.service.balance.ClientBalanceService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientBalanceController {
    private final ClientBalanceService clientBalanceService;

    @GetMapping("/api/clients-balance")
    public ResponseEntity<List<ClientBalanceDto>> getClientBalance() {
        var result = clientBalanceService.getClientBalances();
        return ResponseEntity.ok(result);
    }
}
