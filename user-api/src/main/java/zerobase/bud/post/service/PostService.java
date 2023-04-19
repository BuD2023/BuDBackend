package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.util.Constants.POSTS;

import com.querydsl.core.types.Order;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.service.SendNotificationService;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.PostLike;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.SearchMyPagePost;
import zerobase.bud.post.dto.SearchPost;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostLikeRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.PostRepositoryQuerydslImpl;
import zerobase.bud.post.type.PostSortType;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    private final PostLikeRepository postLikeRepository;

    private final ImageRepository imageRepository;

    private final PostRepositoryQuerydslImpl postRepositoryQuerydsl;

    private final AwsS3Api awsS3Api;

    private final SendNotificationService sendNotificationService;


    @Transactional
    public String createPost(
        Member member
        , List<MultipartFile> images
        , Request request
    ) {

        Post post = postRepository.save(Post.of(member, request));

        saveImageWithPost(images, post);

        sendNotificationService.sendCreatePostNotification(member, post);

        return request.getTitle();
    }

    @Transactional
    public String updatePost(
        List<MultipartFile> images
        , UpdatePost.Request request
    ) {

        Post post = postRepository.findByIdAndPostStatus(request.getPostId(), PostStatus.ACTIVE)
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        post.update(request);

        deleteImages(post.getId());
        imageRepository.deleteAllByPostId(post.getId());

        saveImageWithPost(images, post);

        return request.getTitle();
    }

    public Page<SearchPost.Response> searchPosts(Member member,
                                                 String keyword,
                                                 PostSortType sort,
                                                 Order order,
                                                 int page,
                                                 int size,
                                                 PostType postType) {

        Page<PostDto> posts = postRepositoryQuerydsl.findAllByPostStatus(member.getId(),
                keyword, sort, order, PageRequest.of(page, size), postType);

        return new PageImpl<>(
                posts.stream()
                        .map(post -> SearchPost.Response.of(post,
                                imageRepository.findAllByPostId(post.getId())))
                        .collect(Collectors.toList()),
                posts.getPageable(),
                posts.getTotalElements());
    }

    public Page<SearchMyPagePost.Response> searchMyPagePosts(Member member,
                                                             Long myPageUserId,
                                                             Pageable pageable) {

        Page<PostDto> posts = postRepositoryQuerydsl
                .findAllByMyPagePost(member.getId(), myPageUserId, pageable);

        return new PageImpl<>(
                posts.stream()
                        .map(post -> SearchMyPagePost.Response.of(post,
                                imageRepository.findAllByPostId(post.getId())))
                        .collect(Collectors.toList()),
                pageable,
                posts.getTotalElements()
        );
    }

    public SearchPost.Response searchPost(Member member, Long postId) {
        PostDto postDto = postRepositoryQuerydsl.findByPostId(member.getId(), postId)
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        return SearchPost.Response.of(postDto,
            imageRepository.findAllByPostId(postId));
    }

    public Long deletePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        post.setPostStatus(PostStatus.INACTIVE);
        postRepository.save(post);

        return post.getId();
    }

    public boolean isLike(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        var isAdd = new AtomicReference<>(false);

        postLikeRepository.findByPostIdAndMemberId(postId, member.getId()).ifPresentOrElse(
                        postLike -> removeLike(postLike, post),
                        () -> isAdd.set(addLike(post, member)));

        postRepository.save(post);

        return isAdd.get();
    }

    private void removeLike(PostLike postLike, Post post) {
        postLikeRepository.delete(postLike);

        post.likeCountDown();
    }

    private boolean addLike(Post post, Member member) {
        postLikeRepository.save(PostLike.builder()
                .post(post)
                .member(member)
                .build());

        post.likeCountUp();

        sendNotificationService.sendAddLikeNotification(member, post);

        return true;
    }

    private void saveImageWithPost(List<MultipartFile> images, Post post) {
        if (Objects.nonNull(images)) {
            for (MultipartFile image : images) {
                saveImage(post, image);
            }
        }
    }

    private void saveImage(Post post, MultipartFile image) {
        String imagePath = awsS3Api.uploadImage(image, POSTS);
        imageRepository.save(Image.builder()
            .post(post)
            .imagePath(imagePath)
            .build());
    }

    private void deleteImages(Long postId) {
        List<Image> imageList = imageRepository.findAllByPostId(postId);
        for (Image image : imageList) {
            awsS3Api.deleteImage(image.getImagePath());
        }
    }
}

