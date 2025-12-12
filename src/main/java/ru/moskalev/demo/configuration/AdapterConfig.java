package ru.moskalev.demo.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.moskalev.demo.integration.AccountRepositoryPort;
import ru.moskalev.demo.integration.EmailPort;
import ru.moskalev.demo.integration.adapters.GrpcPhoneNumberAdapter;
import ru.moskalev.demo.integration.adapters.WebclientPhoneNumberAdapter;
import ru.moskalev.demo.service.aggrigation.ClientAggregationCoreService;

@Configuration
public class AdapterConfig {

    @Bean
    @Qualifier("httpAggregationService")
    public ClientAggregationCoreService httpAggregationService(
            AccountRepositoryPort repositoryPort,
            WebclientPhoneNumberAdapter phoneNumberAdapter,
            EmailPort emailPort) {
        return new ClientAggregationCoreService(repositoryPort, phoneNumberAdapter, emailPort);
    }

    @Bean
    @Qualifier("grpcAggregationService")
    public ClientAggregationCoreService grpcAggregationService(
            AccountRepositoryPort repositoryPort,
            GrpcPhoneNumberAdapter phoneNumberAdapter,
            EmailPort emailPort) {
        return new ClientAggregationCoreService(repositoryPort, phoneNumberAdapter, emailPort);
    }
}

