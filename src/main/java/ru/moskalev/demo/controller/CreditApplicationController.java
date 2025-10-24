package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.domain.credit.CreditApplicationDto;
import ru.moskalev.demo.service.credit.CreditProcessService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CreditApplicationController {
    private final CreditProcessService creditProcessServiceOneProducerService;
    private final CreditProcessService creditProcessServiceMultipleProducerService;

    @PostMapping("/api/credit")
    public String submitApplications(@RequestBody List<CreditApplicationDto> applications){
        return creditProcessServiceOneProducerService.submitApplication(applications);
    }

    @PostMapping("/api/credit/multiple-producer")
    public String submitApplicationsMultipleProducer(@RequestBody List<CreditApplicationDto> applications){
        return creditProcessServiceMultipleProducerService.submitApplication(applications);
    }
}
