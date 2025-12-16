package ru.moskalev.demo.controller.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.demo.domain.dto.BankAccountCreateDto;
import ru.moskalev.demo.domain.dto.BankAccountDto;
import ru.moskalev.demo.service.jpaproblem.JpaProblemsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jpa/problems/")
public class JpaProblemsController {
    private final JpaProblemsService jpaProblemsService;

    @GetMapping("/accounts-bad")
    private List<BankAccountDto> getAccountsWithNPlusOne() {
        return jpaProblemsService.getAccountsWithNPlusOne();
    }

    @GetMapping("/accounts-good")
    private List<BankAccountDto> getAccountsWithoutNPlusOne() {
        return jpaProblemsService.getAccountsWithoutNPlusOne();
    }

    @PostMapping("/create-acc")
    public BankAccountCreateDto createAccountsWithInfo(@RequestBody BankAccountCreateDto dto){
        return jpaProblemsService.createAccountsWithInfo(dto);
    }

}
