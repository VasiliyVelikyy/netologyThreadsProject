package ru.moskalev.demo.controller.demostrate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.exproblem.ProcessVolatileService;

@RestController
public class VolatileExampleController {
    private final ProcessVolatileService proccessVolatileService;

    public VolatileExampleController(ProcessVolatileService proccessVolatileService) {
        this.proccessVolatileService = proccessVolatileService;
    }

    @GetMapping("/write-and-read-volatile")
    public String processWriteAndReadVolatile() throws InterruptedException {
        return proccessVolatileService.processWriteAndReadVolatile();
    }

    @GetMapping("/volatile-race-condition")
    public String processRaceCondition() throws InterruptedException {
        return proccessVolatileService.processRaceCondition();
    }
}
