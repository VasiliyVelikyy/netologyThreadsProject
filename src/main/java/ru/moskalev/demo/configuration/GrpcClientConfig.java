package ru.moskalev.demo.configuration;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.moskalev.demo.AccountServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @GrpcClient("account-service")
    private AccountServiceGrpc.AccountServiceFutureStub accountServiceStub;

    @Bean
    public AccountServiceGrpc.AccountServiceFutureStub accountServiceGrpcStub(){
        return accountServiceStub;
    }
}
