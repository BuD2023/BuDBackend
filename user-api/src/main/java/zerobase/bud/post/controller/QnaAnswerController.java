package zerobase.bud.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.CreateQnaAnswer;
import zerobase.bud.post.dto.SearchQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.service.QnaAnswerService;

import javax.validation.Valid;

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

    @GetMapping
    public ResponseEntity<Page<SearchQnaAnswer.Response>> searchQnaAnswers(
            @RequestParam @Valid Long postId,
            @PageableDefault(size = 5, sort = "DATE" ,
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(qnaAnswerService.searchQnaAnswers(postId, pageable));
    }

    @DeleteMapping("/{qna-answer-id}")
    public void deleteQnaAnswer(
            @PathVariable("qna-answer-id") Long qnaAnswerId
    ) {
        qnaAnswerService.deleteQnaAnswer(qnaAnswerId);
    }
}
