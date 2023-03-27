package zerobase.bud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class TestController {
    @GetMapping("/")
    public Map<String, Object> greeting() {
        return Collections.singletonMap("message", "Hello, World");
    }
}
