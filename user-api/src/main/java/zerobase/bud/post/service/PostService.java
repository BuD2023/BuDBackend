package zerobase.bud.post.service;

import com.querydsl.core.types.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awss3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.PostLike;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostLikeRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.PostRepositoryQuerydslImpl;
import zerobase.bud.post.type.PostSortType;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.repository.GithubInfoRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.util.Constants.POSTS;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final GithubInfoRepository githubInfoRepository;

    private final PostRepository postRepository;

    private final PostLikeRepository postLikeRepository;

    private final ImageRepository imageRepository;

    private final PostRepositoryQuerydslImpl postRepositoryQuerydsl;

    private final AwsS3Api awsS3Api;

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
                saveImage(post, image);
            }
        }

        return request.getTitle();
    }

    @Transactional
    public String updatePost(
        List<MultipartFile> images
        , UpdatePost.Request request
    ) {

        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        post.update(request);

        deleteImagesFromAwsS3(post.getId());
        imageRepository.deleteAllByPostId(post.getId());

        if (Objects.nonNull(images)) {
            for (MultipartFile image : images) {
                saveImage(post, image);
            }
        }

        return request.getTitle();
    }

    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String keyword, PostSortType sort,
                                     Order order, int page, int size) {

        Page<Post> posts = postRepositoryQuerydsl.findAllByPostStatus(keyword,
                sort, order, PageRequest.of(page, size));

        return new PageImpl<>(
                posts.stream()
                        .map(post -> PostDto.fromEntity(post,
                                imageRepository.findAllByPostId(post.getId())))
                        .collect(Collectors.toList()),
                posts.getPageable(),
                posts.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostDto searchPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        return PostDto.fromEntity(post, imageRepository.findAllByPostId(postId));
    }

    @Transactional
    public Long deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        post.setPostStatus(PostStatus.INACTIVE);
        postRepository.save(post);

        return post.getId();
    }

    @Transactional
    public boolean isLike(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        var isAdd = new AtomicReference<Boolean>(false);

        postLikeRepository.findByPostIdAndMemberId(postId, member.getId()).ifPresentOrElse(
                        postLike -> removeLike(postLike, post),
                        () -> isAdd.set(addLike(post, member, isAdd.get())));

        postRepository.save(post);

        return isAdd.get();
    }

    private void removeLike(PostLike postLike, Post post) {
        postLikeRepository.delete(postLike);

        post.likeCountDown();
    }

    private boolean addLike(Post post, Member member, Boolean isAdd) {
        postLikeRepository.save(PostLike.builder()
                .post(post)
                .member(member)
                .build());

        post.likeCountUp();

        return true;
    }

    private void saveImage(Post post, MultipartFile image) {
        String imagePath = awsS3Api.uploadImage(image, POSTS);
        imageRepository.save(Image.builder()
            .post(post)
            .imagePath(imagePath)
            .build());
    }

    private void deleteImagesFromAwsS3(Long postId) {
        List<Image> imageList = imageRepository.findAllByPostId(postId);
        for (Image image : imageList) {
            awsS3Api.deleteImage(image.getImagePath());
        }
    }
}
