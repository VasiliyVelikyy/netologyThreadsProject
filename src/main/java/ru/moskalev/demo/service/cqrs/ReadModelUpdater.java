package ru.moskalev.demo.service.cqrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.domain.account.AccountUpdateEvent;
import ru.moskalev.demo.domain.clientinfo.ClientViewUpdate;
import ru.moskalev.demo.integration.client.PhoneNumberClient;
import ru.moskalev.demo.repository.ClientViewRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadModelUpdater {
    private final ClientViewRepository clientViewRepository;
    private final PhoneNumberClient phoneNumberClient;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "${account.deposit.topic}", groupId = "bankaccount-read-model-udpater")
    public void onAccountUpdated(String eventJson) {
        AccountUpdateEvent event;
        try {
            event = objectMapper.readValue(eventJson, AccountUpdateEvent.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        String phone = phoneNumberClient.getPhoneNumBlockedRest(event.accNumber());
        ClientViewUpdate viewUpdate = new ClientViewUpdate(event.accNumber(), event.balance(), phone, event.timestamp());
        clientViewRepository.save(viewUpdate);
    }

}
