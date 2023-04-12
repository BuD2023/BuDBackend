package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.util.Constants.POSTS;

import com.querydsl.core.types.Order;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awss3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.PostRepositoryQuerydslImpl;
import zerobase.bud.post.type.PostSortType;
import zerobase.bud.post.type.PostStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    private final ImageRepository imageRepository;

    private final PostRepositoryQuerydslImpl postRepositoryQuerydsl;

    private final AwsS3Api awsS3Api;

    @Transactional
    public String createPost(Member member, List<MultipartFile> images,
        Request request) {

        Post post = postRepository.save(Post.builder()
            .member(member)
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

        return PostDto.fromEntities(posts, imageRepository);
    }

    @Transactional(readOnly = true)
    public PostDto searchPost(Long postId) {

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        return PostDto.fromEntity(post,
            imageRepository.findAllByPostId(postId));
    }

    @Transactional
    public Long deletePost(Long postId) {

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        post.setPostStatus(PostStatus.INACTIVE);
        postRepository.save(post);

        return post.getId();
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
