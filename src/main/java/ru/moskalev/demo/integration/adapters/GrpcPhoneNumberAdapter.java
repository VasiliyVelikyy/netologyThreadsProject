package ru.moskalev.demo.integration.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.integration.PhoneNumberPort;
import ru.moskalev.demo.integration.client.PhoneNumberClient;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class GrpcPhoneNumberAdapter implements PhoneNumberPort {
    private final PhoneNumberClient phoneNumberClient;

    @Override
    public CompletableFuture<String> getPhoneNumberAsync(String accNumber) {
        return phoneNumberClient.getPhoneNumberAsyncGrpc(accNumber);
    }
}
