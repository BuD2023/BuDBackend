package zerobase.bud.github.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/github")
public class GithubController {

    private final GithubService githubService;

    @PostMapping
    public ResponseEntity<String> saveCommitInfoFromLastCommitDate(
        @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(githubService.saveCommitInfoFromLastCommitDate(
                member
            )
        );
    }

    @GetMapping
    public CommitHistoryInfo getCommitInfo(
        @AuthenticationPrincipal Member member
    ) {
        return githubService.getCommitInfo(member);
    }
}
