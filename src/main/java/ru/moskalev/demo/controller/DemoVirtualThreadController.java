package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.DemoVirtualThreadService;

@RestController
@RequiredArgsConstructor
public class DemoVirtualThreadController {
    private final DemoVirtualThreadService service;

    @GetMapping("/api/demo/vt")
    public String demo(){
        return service.demo();
    }
}
