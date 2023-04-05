package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;
import static zerobase.bud.post.type.PostStatus.ACTIVE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.github.domain.GithubInfo;
import zerobase.bud.github.repository.GithubInfoRepository;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final GithubInfoRepository githubInfoRepository;
    private final PostRepository postRepository;

    private final ImageRepository imageRepository;

    @Transactional
    public String createPost(String email, Request request) {
        GithubInfo githubInfo = githubInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        Post post = postRepository.save(Post.builder()
            .member(githubInfo.getMember())
            .title(request.getTitle())
            .content(request.getContent())
            .postStatus(ACTIVE)
            .postType(request.getPostType())
            .build());

        if (!request.getImageUrl().isEmpty()) {
            imageRepository.save(Image.builder()
                .post(post)
                .imageUrl(request.getImageUrl())
                .build());
        }

        return email;
    }
}
