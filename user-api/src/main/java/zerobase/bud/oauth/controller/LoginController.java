package zerobase.bud.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.oauth.service.LoginService;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    @GetMapping("/token")
    public ResponseEntity<?> requestCode(@RequestParam(value = "code", required = false) String code) {
        String token = loginService.codeToJwt(code);
        if(ObjectUtils.isEmpty(token)) return ResponseEntity.ok(null);

        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, token).build();
    }

}
