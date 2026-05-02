package bookcircle.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {

    @GetMapping
    public String hello() {
        return """
                Bookcircle API is running. Please access /auth, /users, /books, or /comments endpoints.
                """;
    }
}
