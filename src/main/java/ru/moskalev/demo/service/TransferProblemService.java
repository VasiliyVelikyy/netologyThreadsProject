package ru.moskalev.demo.service;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.repository.BankAccountRepository;


@Service
public class TransferProblemService {
    private  final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;

    public TransferProblemService(BankAccountService bankAccountService, BankAccountRepository bankAccountRepository) {
        this.bankAccountService = bankAccountService;
        this.bankAccountRepository = bankAccountRepository;
    }


}
