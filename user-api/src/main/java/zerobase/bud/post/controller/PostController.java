package zerobase.bud.post.controller;

import com.querydsl.core.types.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.post.service.PostService;
import zerobase.bud.post.type.PostSortType;

import javax.validation.Valid;
import java.util.List;

import static zerobase.bud.common.util.Constants.TOKEN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    private final TokenProvider tokenProvider;

    private static final String IMAGES = "images";
    private static final String CREATE_POST_REQUEST = "createPostRequest";
    private static final String UPDATE_POST_REQUEST = "updatePostRequest";


    @PostMapping
    public ResponseEntity<String> createPost(
        @RequestPart(value = IMAGES, required = false) List<MultipartFile> images,
        @RequestPart(value = CREATE_POST_REQUEST) @Valid CreatePost.Request request,
        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok(postService.createPost(
                tokenProvider.getUserId(token.substring(TOKEN_PREFIX.length()))
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
        return ResponseEntity.ok(postService.updatePost(
                images
                , request
            )
        );
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "DATE") PostSortType sort,
            @RequestParam(required = false, defaultValue = "DESC") Order order,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.searchPosts(keyword, sort, order, page, size));
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
}
