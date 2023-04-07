package zerobase.bud.post.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community/post")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<String> createPost(
        @RequestBody @Valid CreatePost.Request request
    ) {
        return ResponseEntity.ok(postService.createPost(
                "khg2154@naver.com"
                , request
            )
        );
    }
}