package zerobase.bud.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.dto.JwtDto;
import zerobase.bud.jwt.dto.RefreshDto;
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

    @PostMapping("/addInfo")
    public ResponseEntity<Boolean> addInfo(@AuthenticationPrincipal Member member,
                                     @RequestPart(required = false) MultipartFile file,
                                     @RequestPart(required = false) String nickname,
                                     @RequestPart String job) {
        return ResponseEntity.ok(authService.addAdditionalInfo(member, file, nickname, job));
    }
}
