package ru.moskalev.demo.controller.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.jpaproblem.MongoDataService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mongo")
public class MongoDataController {
    private  final MongoDataService mongoDataService;

    @PostMapping("/init")
    public String initMongoDb(@RequestParam(defaultValue = "true") boolean clear){
        return mongoDataService.initMongoDb(clear);
    }
}
