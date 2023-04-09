package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;
import static zerobase.bud.post.type.PostStatus.ACTIVE;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.repository.GithubInfoRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final GithubInfoRepository githubInfoRepository;
    private final PostRepository postRepository;

    private final ImageRepository imageRepository;

    @Transactional
    public String createPost(String userId, List<MultipartFile> images,
        Request request) {
        GithubInfo githubInfo = githubInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        Post post = postRepository.save(Post.builder()
            .member(githubInfo.getMember())
            .title(request.getTitle())
            .content(request.getContent())
            .postStatus(ACTIVE)
            .postType(request.getPostType())
            .build());

        if (Objects.nonNull(images)) {
            for (MultipartFile image : images) {
                imageRepository.save(Image.builder()
                    .post(post)
                    .imageUrl(image.getOriginalFilename())
                    .build());
            }
        }

        return githubInfo.getEmail();
    }

}
