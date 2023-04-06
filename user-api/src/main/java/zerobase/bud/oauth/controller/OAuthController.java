package zerobase.bud.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.common.dto.JwtDto;
import zerobase.bud.common.dto.RefreshDto;
import zerobase.bud.member.service.AuthService;

@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final AuthService authService;
    @GetMapping("/login/oauth2")
    private ResponseEntity<JwtDto> login(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return ResponseEntity.ok(authService.login(oAuth2User));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> tokenRefreshRequest(@RequestBody RefreshDto refreshDto) {
        JwtDto result = authService.refresh(refreshDto);
        return !ObjectUtils.isEmpty(result) ?
                ResponseEntity.ok("토큰 재발급에 성공하였습니다.\n" + result) :
                ResponseEntity.ok("토큰 재발급에 실패하였습니다.");
    }
}
