package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.demo.domain.ClientBalanceDto;
import ru.moskalev.demo.integration.kafka.KafkaProducerService;
import ru.moskalev.demo.service.account.BankAccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final KafkaProducerService kafkaProducerService;

    @GetMapping("/client/{accNum}")
    public ResponseEntity<ClientBalanceDto> getAccByNum(@PathVariable String accNum) {
        var result = bankAccountService.getAccountByNum(accNum);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("client/{accNum}")
    public ResponseEntity<ClientBalanceDto> updateAcc(@PathVariable String accNum,
                                                      @RequestParam BigDecimal balance) {
        var result = bankAccountService.updateAcc(accNum, balance.toBigInteger().doubleValue());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/clients-balance")
    public ResponseEntity<List<ClientBalanceDto>> getClientBalance() {
        var result = bankAccountService.getClientBalances();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/evict-client-cache")
    public ResponseEntity<String> evictCache() {
        bankAccountService.evictCache();
        return ResponseEntity.ok("ok");
    }


    @PatchMapping("/save-client-cache-redis/{accNum}")
    public ResponseEntity<String> saveClientBalanceToRedis(@PathVariable String accNum,
                                                           @RequestParam BigDecimal balance) {
        bankAccountService.saveClientBalanceToRedis(accNum, balance.doubleValue());
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/client/sendkafka")
    public ResponseEntity<String> createAccount(@RequestBody Map<String,String> payload){
        String accountNumber = payload.get("accountNumber");
        kafkaProducerService.sendAccountCreated(accountNumber);
        return ResponseEntity.ok("ok");    }

}
