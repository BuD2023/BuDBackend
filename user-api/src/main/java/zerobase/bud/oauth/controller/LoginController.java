package zerobase.bud.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.oauth.service.LoginService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    @GetMapping("/token")
    public ResponseEntity<?> requestCode(@RequestParam(value = "code", required = false) String code) {
        List<String> tokenInfo = loginService.codeToJwt(code);
        if(ObjectUtils.isEmpty(tokenInfo.get(0))) return ResponseEntity.ok(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, tokenInfo.get(0))
                .header("JWT_USER_INFORMATION", tokenInfo.get(1))
                .build();
    }
}
