package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.ProcessAtomicExampleService;

@RestController
public class AtomicExampleController {
    public final ProcessAtomicExampleService processAtomicExampleService;

    public AtomicExampleController(ProcessAtomicExampleService processAtomicExampleService) {
        this.processAtomicExampleService = processAtomicExampleService;
    }

    @GetMapping("/atomic-examples")
    public String processAtomic() throws InterruptedException {
        return processAtomicExampleService.processAtomic();
    }

}
