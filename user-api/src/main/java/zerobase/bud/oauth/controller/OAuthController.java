package zerobase.bud.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final AuthService authService;
    @GetMapping("/login/oauth2")
    public void callback(@AuthenticationPrincipal OAuth2User oAuth2User, HttpServletRequest request, HttpServletResponse response) throws IOException {
        JwtDto token = authService.login(oAuth2User);

        response.setHeader(HttpHeaders.AUTHORIZATION, token.getGrantType() + token.getAccessToken());
        response.setHeader("X-Refresh-Token", token.getGrantType() + token.getRefreshToken());

        response.sendRedirect("http://127.0.0.1:5173/");
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
                                     @RequestPart String nickname,
                                     @RequestPart String job) {
        return ResponseEntity.ok(authService.addAdditionalInfo(member, file, nickname, job));
    }

    @GetMapping("/checkNickname")
    public ResponseEntity<Boolean> checkNickname(@RequestBody String nickname) {
        return ResponseEntity.ok(authService.checkNickname(nickname));
    }
}
