package ru.moskalev.demo.service.demonstrate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmail;
import ru.moskalev.demo.integration.AccountRepositoryPort;
import ru.moskalev.demo.integration.EmailPort;
import ru.moskalev.demo.integration.PhoneNumberPort;
import ru.moskalev.demo.integration.client.PhoneNumberClient;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.email.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemonstrateRestGrpcService {
    private final AccountRepositoryPort accountRepository;
    private final EmailPort emailPort;
    private final PhoneNumberClient phoneNumberClient;

    public List<ClientFullInfoWithEmail> getInfoBlockingRest() {
        return accountRepository.findAllAccounts().stream()
                .map(acc->{
                    String accNum = acc.getAccountNumber();
                    var balance = acc.getBalance();

                    String phone=phoneNumberClient.getPhoneNumBlockedRest(accNum);
                    String email = emailPort.findEmail(accNum);
                    return new ClientFullInfoWithEmail(accNum, balance, phone, email);
                }).toList();
    }

    public List<ClientFullInfoWithEmail> getInfoBlockingGrpc() {
        return accountRepository.findAllAccounts().stream()
                .map(acc->{
                    String accNum = acc.getAccountNumber();
                    var balance = acc.getBalance();

                    String phone = phoneNumberClient.getPhoneNumGrpc(accNum);
                    String email = emailPort.findEmail(accNum);
                    return new ClientFullInfoWithEmail(accNum, balance, phone, email);
                }).toList();
    }
}
