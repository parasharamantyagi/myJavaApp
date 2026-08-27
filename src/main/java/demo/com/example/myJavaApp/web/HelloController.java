package demo.com.example.myJavaApp.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.com.example.myJavaApp.question.QuestionAnswerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final QuestionAnswerService questionAnswerService;

    // Constructor injection: Spring passes the service in, so the controller
    // never builds its own dependencies and stays easy to test.
    HelloController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    @GetMapping("/hello")
    public List<QuestionAnswerResponse> hello() {
        return questionAnswerService.findAll()
                .stream()
                .map(QuestionAnswerResponse::from)
                .toList();
    }

    @PostMapping("/items")
    public ResponseEntity<Item> create(@Valid @RequestBody CreateItemRequest req) {
        // read the body fields — record accessors, no "get" prefix
        String name = req.name();
        BigDecimal price = req.price();

        Item saved = new Item(idGenerator.getAndIncrement(), name, price);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}