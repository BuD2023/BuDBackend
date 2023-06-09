package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.CHANGE_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_POST_OWNER;
import static zerobase.bud.util.Constants.POSTS;

import com.querydsl.core.types.Order;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.PostLike;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.SearchMyPagePost;
import zerobase.bud.post.dto.SearchPost;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.notification.event.like.AddLikePostEvent;
import zerobase.bud.notification.event.create.CreatePostEvent;
import zerobase.bud.post.repository.*;
import zerobase.bud.post.type.PostSortType;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    private final QnaAnswerRepository qnaAnswerRepository;

    private final PostLikeRepository postLikeRepository;

    private final ImageRepository imageRepository;

    private final PostQuerydsl postQuerydsl;

    private final PostImageQuerydsl postImageQuerydsl;

    private final AwsS3Api awsS3Api;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String createPost(
            Member member
            , List<MultipartFile> images
            , Request request
    ) {

        Post post = postRepository.save(Post.of(member, request));

        saveImages(images, post);

        eventPublisher.publishEvent(new CreatePostEvent(member, post));

        return request.getTitle();
    }

    @Transactional
    public String updatePost(
            Long postId
            , List<MultipartFile> images
            , UpdatePost.Request request
            , Member member
    ) {

        Post post = postRepository.findByIdAndPostStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        if(!Objects.equals(post.getMember().getId() , member.getId())){
            throw new BudException(NOT_POST_OWNER);
        }

        if(Objects.nonNull(post.getQnaAnswerPin())){
            throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
        }

        post.update(request);

        deleteImages(post);

        saveImages(images, post);

        return request.getTitle();
    }

    public Page<SearchPost.Response> searchPosts(Member member,
                                                 String keyword,
                                                 PostSortType sort,
                                                 Order order,
                                                 int page,
                                                 int size,
                                                 PostType postType) {

        Page<PostDto> posts = postQuerydsl.findAllByPostStatus(member.getId(),
                keyword, sort, order, PageRequest.of(page, size), postType);

        return new PageImpl<>(
                posts.stream()
                        .map(post -> SearchPost.Response.of(post,
                                postImageQuerydsl
                                        .findImagePathAllByPostId(post.getId())))
                        .collect(Collectors.toList()),
                posts.getPageable(),
                posts.getTotalElements());
    }

    public Page<SearchMyPagePost.Response> searchMyPagePosts(Member member,
                                                             Long myPageUserId,
                                                             PostType postType,
                                                             Pageable pageable) {

        Page<PostDto> posts = postQuerydsl.findAllByMyPagePost(
                member.getId(),
                myPageUserId,
                postType,
                pageable
        );

        return new PageImpl<>(
                posts.stream()
                        .map(post -> SearchMyPagePost.Response.of(post,
                                postImageQuerydsl.findImagePathAllByPostId(post.getId())))
                        .collect(Collectors.toList()),
                pageable,
                posts.getTotalElements()
        );
    }

    public SearchPost.Response searchPost(Member member, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        PostDto postDto =
                postQuerydsl.findByPostId(member.getId(), postId);

        post.hitCountUp();

        postRepository.save(post);

        return SearchPost.Response.of(postDto,
                postImageQuerydsl.findImagePathAllByPostId(postId));
    }

    @Transactional
    public Long deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        deleteFeedImagesFromS3(post);

        if (Objects.equals(post.getPostType(), PostType.QNA)) {
            List<QnaAnswer> qnaAnswers = qnaAnswerRepository.findAllByPostId(postId);

            qnaAnswers.forEach(this::deleteQnaAnswerImagesFromS3);
        }

        postRepository.deleteByPostId(post.getId());

        return post.getId();
    }

    @Transactional
    public boolean addLike(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        var isAdd = new AtomicReference<>(false);

        postLikeRepository.findByPostIdAndMemberId(postId, member.getId()).ifPresentOrElse(
                postLike -> cancelLike(postLike, post),
                () -> isAdd.set(addLike(post, member)));

        postRepository.save(post);

        return isAdd.get();
    }

    private void cancelLike(PostLike postLike, Post post) {
        postLikeRepository.delete(postLike);

        post.likeCountDown();
    }

    private boolean addLike(Post post, Member member) {
        postLikeRepository.save(PostLike.builder()
                .post(post)
                .member(member)
                .build());

        post.likeCountUp();

        eventPublisher.publishEvent(new AddLikePostEvent(member, post));

        return true;
    }

    private void saveImages(List<MultipartFile> images, Post post) {
        if (Objects.nonNull(images)) {
            for (MultipartFile image : images) {
                String imagePath = awsS3Api.uploadImage(image, POSTS);
                imageRepository.save(Image.of(post,imagePath));
            }
        }
    }

    private void deleteImages(Post post) {
        deleteFeedImagesFromS3(post);

        imageRepository.deleteAllByPostId(post.getId());
    }

    private void deleteFeedImagesFromS3(Post post) {
        post.getImages()
                .forEach(image -> awsS3Api.deleteImage(image.getImagePath()));
    }

    private void deleteQnaAnswerImagesFromS3(QnaAnswer qnaAnswer) {
        qnaAnswer.getQnaAnswerImages()
                .forEach(image -> awsS3Api.deleteImage(image.getImagePath()));
    }
}

