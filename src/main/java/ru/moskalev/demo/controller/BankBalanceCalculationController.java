package ru.moskalev.demo.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.BankBalanceSummatorService;
import ru.moskalev.demo.service.BankInterestAccrualService;

@RestController
@RequiredArgsConstructor
public class BankBalanceCalculationController {
    private final BankBalanceSummatorService bankBalanceSummator;
    private final BankInterestAccrualService bankInterestAccrualService;

    //12,942 секунд (12942336900 наносекунд)
    @GetMapping("/api/get-sum/all/iteration")
    public Long getSumAllIteration() {
        return bankBalanceSummator.getSumAllAccFullIteration();
    }

    //12,724 секунд
    @GetMapping("/api/get-sum/all/recursive")
    public Long getSumAllRecursive() {
        return bankBalanceSummator.getSumAllAccRecursive();
    }


    @GetMapping("/api/interest/all")
    public void calculateInterest() {
        bankInterestAccrualService.calculateInterest();
    }


}
