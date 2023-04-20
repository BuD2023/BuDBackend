package zerobase.bud.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.bud.comment.service.QnaAnswerCommentService;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.CreateQnaAnswer;
import zerobase.bud.post.dto.SearchQnaAnswer;
import zerobase.bud.post.dto.QnaAnswerCommentDto;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.service.QnaAnswerService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/qna-answers")
public class QnaAnswerController {

    private final QnaAnswerService qnaAnswerService;

    private final QnaAnswerCommentService qnaAnswerCommentService;

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

    @PostMapping("/qna-comments/{qnaCommentId}/like")
    public ResponseEntity<Long> commentLike(@PathVariable Long qnaCommentId,
                                            @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(qnaAnswerCommentService.commentLike(qnaCommentId, member));
    }

    @PostMapping("/qna-comments/{qnaCommentId}/pin")
    public ResponseEntity<Long> commentPin(@PathVariable Long qnaCommentId,
                                           @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(qnaAnswerCommentService.commentPin(qnaCommentId, member));
    }

    @DeleteMapping("/{qnaAnswerId}/qna-comments/pin")
    public ResponseEntity<Long> cancelCommentPin(@PathVariable Long qnaAnswerId,
                                                 @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(qnaAnswerCommentService.cancelCommentPin(qnaAnswerId, member));
    }


    @GetMapping("/{qnaAnswerId}/qna-comments")
    public ResponseEntity<Slice<QnaAnswerCommentDto>> comments(@PathVariable Long qnaAnswerId,
                                                               @AuthenticationPrincipal Member member,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(qnaAnswerCommentService.comments(qnaAnswerId, member, page, size));
    }

    @DeleteMapping("/qna-comments/{qnaCommentId}")
    public ResponseEntity<Long> deleteComment(@PathVariable Long qnaCommentId,
                                              @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(qnaAnswerCommentService.delete(qnaCommentId, member));
    }

    @PostMapping("/{qnaAnswerId}/pin")
    public ResponseEntity<Long> qnaAnswerPin(
        @PathVariable Long qnaAnswerId,
        @AuthenticationPrincipal Member member
    ){
        return ResponseEntity.ok(qnaAnswerService.qnaAnswerPin(qnaAnswerId, member));
    }

    @DeleteMapping("/pin/{qnaAnswerPinId}")
    public ResponseEntity<Long> cancelQnaAnswerPin(
        @PathVariable Long qnaAnswerPinId,
        @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(qnaAnswerService.cancelQnaAnswerPin(qnaAnswerPinId, member));
    }

    @PostMapping("/{qnaAnswerId}/like")
    public ResponseEntity<String> setLike(
            @PathVariable Long qnaAnswerId,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                qnaAnswerService.setLike(qnaAnswerId, member) ? "좋아요" : "좋아요 해제");
    }
}

