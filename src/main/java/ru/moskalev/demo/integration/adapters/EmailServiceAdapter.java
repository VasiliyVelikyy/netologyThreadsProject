package ru.moskalev.demo.integration.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.integration.EmailPort;
import ru.moskalev.demo.service.email.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailPort {
    private final EmailService emailService;

    @Override
    public String findEmail(String accountNumber) {
        return emailService.findEmail(accountNumber);
    }
}
