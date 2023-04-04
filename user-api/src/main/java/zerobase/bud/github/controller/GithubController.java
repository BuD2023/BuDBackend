package zerobase.bud.github.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubApi;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home/github")
public class GithubController {

    private final GithubApi githubApi;

    @PostMapping
    public ResponseEntity<?> saveCommitInfoFromLastCommitDate() {

        return ResponseEntity.ok(githubApi.saveCommitInfoFromLastCommitDate(
            "khg2154@naver.com", "Ggyumalang")
        );
    }

    @GetMapping("/info")
    public CommitHistoryInfo getCommitInfo() throws IOException {
        return githubApi.getCommitInfo("khg2154@naver.com");
    }
}
