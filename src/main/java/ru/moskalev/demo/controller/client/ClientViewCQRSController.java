package ru.moskalev.demo.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfo;
import ru.moskalev.demo.domain.debit.DepositRequest;
import ru.moskalev.demo.service.cqrs.BankAccountCommandService;
import ru.moskalev.demo.service.cqrs.BankAccountQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cqrs")
public class ClientViewCQRSController {
    private final BankAccountCommandService bankAccountCommandService;
    private final BankAccountQueryService queryService;

    @PostMapping("/deposit")
    public ResponseEntity<Void> handleDepositCommand(@RequestBody DepositRequest request) {
        bankAccountCommandService.deposit(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/client/{accountNumber}")
    public ClientFullInfo handleClientQuery(@PathVariable String accountNumber) {
        return queryService.handleClientQuery(accountNumber);

    }
}
