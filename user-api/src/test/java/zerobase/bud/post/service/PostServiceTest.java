package zerobase.bud.post.service;

import static com.querydsl.core.types.Order.ASC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.post.type.PostSortType.HIT;
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.post.type.PostStatus.INACTIVE;
import static zerobase.bud.post.type.PostType.FEED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awss3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Level;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.PostLike;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostLikeRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.PostRepositoryQuerydslImpl;
import zerobase.bud.post.type.PostType;
import zerobase.bud.type.MemberStatus;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepositoryQuerydslImpl postRepositoryQuerydsl;

    @Mock
    private AwsS3Api awsS3Api;

    @InjectMocks
    private PostService postService;

    @Test
    void success_createPostWithImage() {
        //given 어떤 데이터가 주어졌을 때
        List<MultipartFile> images = getMockMultipartFiles();

        given(postRepository.save(any()))
            .willReturn(getPost());

        given(awsS3Api.uploadImage(any(), any()))
            .willReturn("awsS3Image");

        given(imageRepository.save(any()))
            .willReturn(Image.builder()
                .post(getPost())
                .imagePath("imagePath")
                .build());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(
            Image.class);
        //when 어떤 경우에
        String result = postService.createPost(getMember(), images,
            CreatePost.Request.builder()
                .title("t")
                .content("c")
                .postType(PostType.FEED)
                .build());

        //then 이런 결과가 나온다.
        verify(postRepository, times(1)).save(captor.capture());
        verify(imageRepository, times(1)).save(imageCaptor.capture());
        assertEquals("t", captor.getValue().getTitle());
        assertEquals("c", captor.getValue().getContent());
        assertEquals(ACTIVE, captor.getValue().getPostStatus());
        assertEquals(PostType.FEED, captor.getValue().getPostType());
        assertEquals("awsS3Image", imageCaptor.getValue().getImagePath());
        assertEquals("title", imageCaptor.getValue().getPost().getTitle());
        assertEquals("content", imageCaptor.getValue().getPost().getContent());
        assertEquals("t", result);
    }

    @Test
    void success_createPost() {
        //given 어떤 데이터가 주어졌을 때
        List<MultipartFile> images = getMockMultipartFiles();

        given(postRepository.save(any()))
            .willReturn(getPost());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        //when 어떤 경우에
        String result = postService.createPost(getMember(), images,
            CreatePost.Request.builder()
                .title("t")
                .content("c")
                .postType(PostType.QNA)
                .build());

        //then 이런 결과가 나온다.
        verify(postRepository, times(1)).save(captor.capture());
        assertEquals("t", captor.getValue().getTitle());
        assertEquals("c", captor.getValue().getContent());
        assertEquals(ACTIVE, captor.getValue().getPostStatus());
        assertEquals(PostType.QNA, captor.getValue().getPostType());
        assertEquals("t", result);
    }

    @Test
    void success_updatePostWithImage() {
        //given 어떤 데이터가 주어졌을 때
        List<MultipartFile> images = getMockMultipartFiles();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getPost()));

        given(awsS3Api.uploadImage(any(), any()))
            .willReturn("awsS3Image");

        given(imageRepository.save(any()))
            .willReturn(Image.builder()
                .post(getPost())
                .imagePath("updateImageUrl")
                .build());

        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(
            Image.class);
        //when 어떤 경우에
        String result = postService.updatePost(images,
            UpdatePost.Request.builder()
                .postId(1L)
                .title("t")
                .content("c")
                .postType(PostType.QNA)
                .build());

        //then 이런 결과가 나온다.
        verify(imageRepository, times(1)).save(imageCaptor.capture());
        assertEquals("awsS3Image", imageCaptor.getValue().getImagePath());
        assertEquals("t", imageCaptor.getValue().getPost().getTitle());
        assertEquals("c", imageCaptor.getValue().getPost().getContent());
        assertEquals(PostType.QNA,
            imageCaptor.getValue().getPost().getPostType());
        assertEquals("t", result);
    }

    @Test
    @DisplayName("NOT_FOUND_POST_updatePost")
    void NOT_FOUND_POST_updatePost() {
        //given 어떤 데이터가 주어졌을 때
        given(postRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> postService.updatePost(getMockMultipartFiles(),
                UpdatePost.Request.builder()
                    .postId(1L)
                    .title("t")
                    .content("c")
                    .postType(PostType.FEED)
                    .build()));

        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("성공 - 게시물 리스트 검색")
    void success_searchPosts() {
        //given
        List<Post> posts = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            posts.add(Post.builder()
                .id((long) i)
                .title("제목" + i)
                .content("내용" + i)
                .commentCount(i)
                .likeCount(i)
                .scrapCount(i)
                .hitCount(i)
                .postStatus(
                    i == 3 ? INACTIVE : ACTIVE)
                .postType(FEED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        }

        List<Image> images = getImageList(posts.get(0));

        PageRequest pageRequest = PageRequest.of(0, 3);

        given(postRepositoryQuerydsl.findAllByPostStatus(anyString(), any(),
            any(), any()))
            .willReturn(new PageImpl<>(posts, pageRequest, 3));

        given(imageRepository.findAllByPostId(anyLong()))
            .willReturn(images);

        //when
        Page<PostDto> postDtos = postService.searchPosts("제목",
            HIT, ASC, 0, 3);

        //then
        assertEquals(3, postDtos.getContent().size());
        assertEquals(1L, postDtos.getContent().get(0).getId());
        assertEquals("제목1", postDtos.getContent().get(0).getTitle());
//        assertEquals(new String[]{"url1", "url3"}, postDtos.getContent().get(2).getImageUrls());
        assertEquals("내용1", postDtos.getContent().get(0).getContent());
        assertEquals(1, postDtos.getContent().get(0).getHitCount());
        assertEquals(1, postDtos.getContent().get(0).getCommentCount());
        assertEquals(1, postDtos.getContent().get(0).getScrapCount());
        assertEquals(ACTIVE, postDtos.getContent().get(0).getPostStatus());
        assertEquals(FEED, postDtos.getContent().get(0).getPostType());
    }

    @Test
    @DisplayName("성공 - 게시물 상세 데이터 검색")
    void success_searchPost() {
        //given
        Post post = getPost();
        post.setId((long) 1);

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.of(post));

        given(imageRepository.findAllByPostId(anyLong()))
            .willReturn(getImageList(post));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        PostDto postDto = postService.searchPost((long) 1);

        //then
        assertEquals(1L, postDto.getId());
        assertEquals("title", postDto.getTitle());
        assertEquals(Arrays.toString(new String[]{"img", "img", "img"}),
            Arrays.toString(postDto.getImageUrls()));
        assertEquals("content", postDto.getContent());
        assertEquals(ACTIVE, postDto.getPostStatus());
        assertEquals(FEED, postDto.getPostType());
    }

    @Test
    @DisplayName("성공 - 게시물 삭제")
    void success_deletePost() {
        //given
        Post post = getPost();
        post.setId((long) 1);

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.of(post));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        Long id = postService.deletePost((long) 1);

        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        assertEquals(id, post.getId());
        assertEquals(postCaptor.getValue().getPostStatus(), INACTIVE);
    }

    @Test
    @DisplayName("실패 - 게시물 상세 데이터 검색")
    void fail_searchPost() {
        //given
        given(postRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> postService.searchPost((long) 1));

        //then
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("실패 - 게시물 삭제")
    void fail_deletePost() {
        //given
        given(postRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> postService.deletePost((long) 1));

        //then
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("성공 - 게시물 좋아요 추가")
    void success_postLike(){
        //given
        Post post = getPost();
        post.setId((long)1);
        post.setLikeCount(2);

        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .userId("xoals25")
                .profileImg("abcde.jpg")
                .nickname("엄탱")
                .job("백")
                .oAuthAccessToken("token")
                .build();

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .build();

        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));

        given(postLikeRepository.findByPostIdAndMemberId(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        boolean isAdd = postService.isLike(post.getId(), member);

        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        assertEquals(1L, post.getId());
        assertEquals(3, postCaptor.getValue().getLikeCount());
        assertTrue(isAdd);
    }

    @Test
    @DisplayName("성공 - 게시물 좋아요 해제")
    void success_postLikeRemove(){
        //given
        Post post = getPost();
        post.setId((long)1);
        post.setLikeCount(2);

        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .userId("xoals25")
                .profileImg("abcde.jpg")
                .nickname("엄탱")
                .job("백")
                .oAuthAccessToken("token")
                .build();

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .build();

        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));

        given(postLikeRepository.findByPostIdAndMemberId(anyLong(), anyLong()))
                .willReturn(Optional.of(postLike));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        boolean isAdd = postService.isLike(post.getId(), member);

        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        assertEquals(1L, post.getId());
        assertEquals(1, postCaptor.getValue().getLikeCount());
        assertFalse(isAdd);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 게시글 좋아요 ")
    void fail_postLike() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
                () -> postService.isLike((long)1, new Member()));

        //then
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }




    private static Post getPost() {
        return Post.builder()
            .member(getMember())
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();
    }

    private static Member getMember() {
        return Member.builder()
            .nickname("nick")
            .level(getLevel())
            .userId("")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static Level getLevel() {
        return Level.builder()
            .levelCode("씩씩한_새싹")
            .levelStartCommitCount(0)
            .nextLevelStartCommitCount(17)
            .levelNumber(1)
            .build();
    }

    private static List<MultipartFile> getMockMultipartFiles() {
        List<MultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("multipartFile",
            "health.jpg",
            "image/jpg",
            "<<jpeg data>>".getBytes()));
        return images;
    }

    private static List<Image> getImageList(Post post) {
        List<Image> images = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            images.add(Image.builder()
                .id((long) (i + 1))
                .post(post)
                .imagePath("img")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        }

        return images;
    }
}