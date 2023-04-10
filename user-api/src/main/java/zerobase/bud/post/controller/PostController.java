package zerobase.bud.post.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.service.PostService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/community/post")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<String> createPost(
        @RequestBody @Valid CreatePost.Request request
    ) {
        log.error("post start");
        String sting = postService.createPost(
                "khg2154@naver.com"
                , request
        );
        log.error("post end");
        return ResponseEntity.ok(sting);
    }
}
