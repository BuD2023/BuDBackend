package zerobase.bud.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.dto.JwtDto;
import zerobase.bud.member.service.AuthService;

@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final AuthService authService;
    @GetMapping("/login/oauth2")
    private ResponseEntity<JwtDto> login(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return ResponseEntity.ok(authService.login(oAuth2User));
    }

    @PostMapping("/addInfo")
    public ResponseEntity<Boolean> addInfo(@AuthenticationPrincipal Member member,
                                     @RequestPart(required = false) MultipartFile file,
                                     @RequestPart(required = false) String imagePath,
                                     @RequestPart String nickname,
                                     @RequestPart String job) {
        return ResponseEntity.ok(authService.addAdditionalInfo(member, file, nickname, job, imagePath));
    }

    @GetMapping("/checkNickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(authService.checkNickname(nickname));
    }
}
