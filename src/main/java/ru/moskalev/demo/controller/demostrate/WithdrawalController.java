package ru.moskalev.demo.controller.demostrate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.exproblem.ProcessReentrantLockService;

@RestController
public class WithdrawalController {
    private final ProcessReentrantLockService processReentrantLockService;

    public WithdrawalController(ProcessReentrantLockService processReentrantLockService) {
        this.processReentrantLockService = processReentrantLockService;
    }

    @GetMapping("process-withdrawal-large")
    public String processWithdrawalLarge() throws InterruptedException {
        return processReentrantLockService.processWithdrawalLarge();
    }

    @GetMapping("/process-semaphore-withdrawal")
    public String processSemaphoreWithdrawal() throws InterruptedException {
        return processReentrantLockService.processSemaphoreWithdrawal();
    }

    @GetMapping("process-withdraw-deposit-process")
    public String processWithdrawalDeposit() throws InterruptedException {
        return processReentrantLockService.processWithdrawalDeposit();
    }

    @GetMapping("/process-downgrade")
    public String processDowngrade() throws InterruptedException {
        return processReentrantLockService.processDowngrade();
    }
}
