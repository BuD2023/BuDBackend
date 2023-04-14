package zerobase.bud.post.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.CreateQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.service.QnaAnswerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/qna-answer")
public class QnaAnswerController {

    private final QnaAnswerService qnaAnswerService;

    @PostMapping
    public ResponseEntity<String> createQnaAnswer(
        @RequestBody @Valid CreateQnaAnswer.Request request,
        @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(qnaAnswerService.createQnaAnswer(
                member
                , request
            )
        );
    }

    @PutMapping
    public ResponseEntity<Long> updateQnaAnswer(
        @RequestBody @Valid UpdateQnaAnswer.Request request
    ) {
        return ResponseEntity.ok(qnaAnswerService.updateQnaAnswer(request));
    }
}
