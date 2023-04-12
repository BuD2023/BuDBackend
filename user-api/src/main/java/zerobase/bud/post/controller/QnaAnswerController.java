package zerobase.bud.post.controller;

import static zerobase.bud.common.util.Constants.TOKEN_PREFIX;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.post.dto.CreateQnaAnswer;
import zerobase.bud.post.service.QnaAnswerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/answer")
public class QnaAnswerController {

    private final TokenProvider tokenProvider;

    private final QnaAnswerService qnaAnswerService;

    @PostMapping
    public ResponseEntity<String> createQnaAnswer(
        @RequestBody @Valid CreateQnaAnswer.Request request,
        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok(qnaAnswerService.createQnaAnswer(
                tokenProvider.getUserId(token.substring(TOKEN_PREFIX.length()))
                , request
            )
        );
    }
}
