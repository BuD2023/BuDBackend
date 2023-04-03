package zerobase.bud;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class TestController {
    @GetMapping("/")
    public ResponseEntity greeting() {
        return ResponseEntity.ok(Collections.singletonMap("message", "Hello, World"));
    }

    @GetMapping("/error")
    public void error() {
        throw new RuntimeException("error occured");
    }
}
