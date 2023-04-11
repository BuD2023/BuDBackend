package zerobase.bud.github.controller;

import static zerobase.bud.common.util.Constants.TOKEN_PREFIX;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubService;
import zerobase.bud.jwt.TokenProvider;

@RestController
@RequiredArgsConstructor
@RequestMapping("/github")
public class GithubController {

    private final GithubService githubService;

    private final TokenProvider tokenProvider;

    @PostMapping
    public ResponseEntity<String> saveCommitInfoFromLastCommitDate(
        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token
    ) {

        return ResponseEntity.ok(githubService.saveCommitInfoFromLastCommitDate(
                tokenProvider.getUserId(token.substring(TOKEN_PREFIX.length()))
            )
        );
    }

    @GetMapping
    public CommitHistoryInfo getCommitInfo(
        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token
    ) {
        return githubService.getCommitInfo(
            tokenProvider.getUserId(token.substring(TOKEN_PREFIX.length())));
    }
}
