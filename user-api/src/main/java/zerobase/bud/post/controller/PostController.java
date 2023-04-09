package zerobase.bud.post.controller;

import static zerobase.bud.common.util.Constant.TOKEN_PREFIX;

import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.service.PostService;
import zerobase.bud.security.TokenProvider;

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
}
