package zerobase.bud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.common.dto.Response;
import zerobase.bud.common.type.ResponseCode;

import java.util.Collections;
import java.util.Map;

@RestController
public class TestController {
    @GetMapping("/")
    public ResponseEntity greeting() {
        Map data = Collections.singletonMap("message", "Hello, World");
        return new ResponseEntity<>(Response.of(ResponseCode.TEST_OK, data), HttpStatus.OK);
    }

    @GetMapping("/error")
    public void error() {
        throw new RuntimeException("error occured");
    }
}
