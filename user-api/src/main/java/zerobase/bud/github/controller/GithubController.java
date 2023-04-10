package zerobase.bud.github.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/home/github")
public class GithubController {

    private final GithubService githubService;

    @PostMapping
    public ResponseEntity<String> saveCommitInfoFromLastCommitDate(
            @AuthenticationPrincipal OAuth2User oAuth2User
    ) {

        return ResponseEntity.ok(githubService.saveCommitInfoFromLastCommitDate(
//            oAuth2User.getAttribute("email")
                        "khg2154@naver.com")
        );
    }

    @GetMapping("/info")
    public CommitHistoryInfo getCommitInfo() {
        return githubService.getCommitInfo("khg2154@naver.com");
    }
}
