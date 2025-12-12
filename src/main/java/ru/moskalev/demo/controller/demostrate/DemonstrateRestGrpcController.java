package ru.moskalev.demo.controller.demostrate;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmail;
import ru.moskalev.demo.service.demonstrate.DemonstrateRestGrpcService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DemonstrateRestGrpcController {
    private final DemonstrateRestGrpcService demonstrateRestGrpcService;

    @GetMapping("/benchmark/blocking-rest")
    public List<ClientFullInfoWithEmail> blockingRest(){
        return demonstrateRestGrpcService.getInfoBlockingRest();
    }

    @GetMapping("/benchmark/blocking-grpc")
    public List<ClientFullInfoWithEmail> blockingGrpc(){
        return demonstrateRestGrpcService.getInfoBlockingGrpc();
    }
}
