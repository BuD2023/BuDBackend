package zerobase.bud.post.controller;

import com.querydsl.core.types.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.comment.service.CommentService;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.*;
import zerobase.bud.post.service.PostService;
import zerobase.bud.post.service.ScrapService;
import zerobase.bud.post.type.PostSortType;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    private final ScrapService scrapService;

    private final CommentService commentService;

    private static final String IMAGES = "images";
    private static final String CREATE_POST_REQUEST = "createPostRequest";
    private static final String UPDATE_POST_REQUEST = "updatePostRequest";


    @PostMapping
    public ResponseEntity<String> createPost(
            @RequestPart(value = IMAGES, required = false) List<MultipartFile> images,
            @RequestPart(value = CREATE_POST_REQUEST) @Valid CreatePost.Request request,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(postService.createPost(
                        member
                        , images
                        , request
                )
        );
    }

    @PostMapping("/update")
    public ResponseEntity<String> updatePost(
            @RequestPart(value = IMAGES, required = false) List<MultipartFile> images,
            @RequestPart(value = UPDATE_POST_REQUEST) @Valid UpdatePost.Request request
    ) {
        return ResponseEntity.ok(postService.updatePost(images, request));
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "DATE") PostSortType sort,
            @RequestParam(required = false, defaultValue = "DESC") Order order,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                postService.searchPosts(keyword, sort, order, page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> searchPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.searchPost(postId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Long> deletePost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.deletePost(postId));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> setLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                postService.isLike(postId, member) ? "좋아요" : "좋아요 해제");
    }

    @PostMapping("/{postId}/scrap")
    public ResponseEntity<String> setScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(scrapService.isScrap(postId, member)
                ? "스크랩 추가" : "스크랩 해제");
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long postId,
                                                @AuthenticationPrincipal Member member,
                                                @RequestBody String content) {
        return ResponseEntity.ok(commentService.createComment(postId, member, content));
    }

    @PutMapping("/comments/{commentId}/modify")
    public ResponseEntity<CommentDto> modifyComment(@PathVariable Long commentId,
                                                @AuthenticationPrincipal Member member,
                                                @RequestBody String content) {
        return ResponseEntity.ok(commentService.modifyComment(commentId, member, content));
    }

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<RecommentDto> createRecomment(@PathVariable Long commentId,
                                                        @AuthenticationPrincipal Member member,
                                                        @RequestBody String content) {
        return ResponseEntity.ok(commentService.createRecomment(commentId, member, content));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Long> commentLike(@PathVariable Long commentId,
                                            @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(commentService.commentLike(commentId, member));
    }

    @PostMapping("/comments/{commentId}/pin")
    public ResponseEntity<Long> commentPin(@PathVariable Long commentId,
                                           @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(commentService.commentPin(commentId, member));
    }

    @DeleteMapping("/{postId}/comments/pin")
    public ResponseEntity<Long> cancelCommentPin(@PathVariable Long postId,
                                                 @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(commentService.cancelCommentPin(postId, member));
    }


    @GetMapping("/{postId}/comments")
    public ResponseEntity<Slice<CommentDto>> comments(@PathVariable Long postId,
                                                      @AuthenticationPrincipal Member member,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.comments(postId, member, page, size));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Long> deleteComment(@PathVariable Long commentId,
                                                 @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(commentService.delete(commentId, member));
    }
}

