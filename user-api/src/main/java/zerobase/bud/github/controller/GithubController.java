package zerobase.bud.github.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home/github")
public class GithubController {

    private final GithubService githubService;

    @PostMapping
    public ResponseEntity<String> saveCommitInfoFromLastCommitDate() {

        return ResponseEntity.ok(githubService.saveCommitInfoFromLastCommitDate(
            "khg2154@naver.com", "Ggyumalang")
        );
    }

    @GetMapping("/info")
    public CommitHistoryInfo getCommitInfo() {
        return githubService.getCommitInfo("khg2154@naver.com");
    }
}
