package zerobase.bud.post.service;

import static com.querydsl.core.types.Order.ASC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.common.type.ErrorCode.CHANGE_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_POST_OWNER;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Level;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.service.SendNotificationService;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.PostLike;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.domain.QnaAnswerPin;
import zerobase.bud.post.domain.Scrap;
import zerobase.bud.post.dto.*;
import zerobase.bud.post.repository.*;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;
import zerobase.bud.type.MemberStatus;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ScrapRepository scrapRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepositoryQuerydslImpl postRepositoryQuerydsl;

    @Mock
    private AwsS3Api awsS3Api;

    @Mock
    private SendNotificationService sendNotificationService;

    @InjectMocks
    private PostService postService;

    @InjectMocks
    private ScrapService scrapService;

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
                        .title("resultTitle")
                        .content("resultContent")
                        .postType(PostType.FEED)
                        .build());

        //then 이런 결과가 나온다.
        verify(postRepository, times(1)).save(captor.capture());
        verify(imageRepository, times(1)).save(imageCaptor.capture());
        assertEquals("resultTitle", captor.getValue().getTitle());
        assertEquals("resultContent", captor.getValue().getContent());
        assertEquals(ACTIVE, captor.getValue().getPostStatus());
        assertEquals(PostType.FEED, captor.getValue().getPostType());
        assertEquals("awsS3Image", imageCaptor.getValue().getImagePath());
        assertEquals("title", imageCaptor.getValue().getPost().getTitle());
        assertEquals("content", imageCaptor.getValue().getPost().getContent());
        assertEquals("resultTitle", result);
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
                        .title("resultTitle")
                        .content("resultContent")
                        .postType(PostType.FEED)
                        .build());

        //then 이런 결과가 나온다.
        verify(postRepository, times(1)).save(captor.capture());
        assertEquals("resultTitle", captor.getValue().getTitle());
        assertEquals("resultContent", captor.getValue().getContent());
        assertEquals(ACTIVE, captor.getValue().getPostStatus());
        assertEquals(PostType.FEED, captor.getValue().getPostType());
        assertEquals("resultTitle", result);
    }

    @Test
    void success_updatePostWithImage() {
        //given 어떤 데이터가 주어졌을 때
        List<MultipartFile> images = getMockMultipartFiles();

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
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
        String result = postService.updatePost(1L, images,
                UpdatePost.Request.builder()
                        .title("resultTitle")
                        .content("resultContent")
                        .postType(PostType.QNA)
                        .build()
                , getMember());

        //then 이런 결과가 나온다.
        verify(imageRepository, times(1)).save(imageCaptor.capture());
        assertEquals("awsS3Image", imageCaptor.getValue().getImagePath());
        assertEquals("resultTitle", imageCaptor.getValue().getPost().getTitle());
        assertEquals("resultContent", imageCaptor.getValue().getPost().getContent());
        assertEquals(PostType.QNA,
                imageCaptor.getValue().getPost().getPostType());
        assertEquals("resultTitle", result);
    }

    @Test
    @DisplayName("NOT_FOUND_POST_updatePost")
    void NOT_FOUND_POST_updatePost() {
        //given 어떤 데이터가 주어졌을 때
        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
                () -> postService.updatePost(1L, getMockMultipartFiles()
                        , UpdatePost.Request.builder()
                                .title("resultTitle")
                                .content("resultContent")
                                .postType(PostType.FEED)
                                .build()
                        , getMember()));

        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_POST_OWNER_updatePost")
    void NOT_POST_OWNER_updatePost() {
        //given 어떤 데이터가 주어졌을 때
        Member diffMember = Member.builder()
                .id(3L)
                .nickname("nick")
                .level(getLevel())
                .userId("")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.ofNullable(getPost()));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
                () -> postService.updatePost(1L, getMockMultipartFiles()
                        , UpdatePost.Request.builder()
                                .title("resultTitle")
                                .content("resultContent")
                                .postType(PostType.FEED)
                                .build()
                        , diffMember));

        //then 이런 결과가 나온다.
        assertEquals(NOT_POST_OWNER, budException.getErrorCode());
    }

    @Test
    @DisplayName("CHANGE_IMPOSSIBLE_PINNED_ANSWER_updatePost")
    void CHANGE_IMPOSSIBLE_PINNED_ANSWER_updatePost() {
        //given 어떤 데이터가 주어졌을 때
        //given 어떤 데이터가 주어졌을 때

        Post post = Post.builder()
                .member(getMember())
                .title("title")
                .content("content")
                .postStatus(ACTIVE)
                .postType(FEED)
                .qnaAnswerPin(getQnaAnswerPin())
                .build();

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.ofNullable(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
                () -> postService.updatePost(1L, getMockMultipartFiles()
                        , UpdatePost.Request.builder()
                                .title("resultTitle")
                                .content("resultContent")
                                .postType(PostType.FEED)
                                .build()
                        , getMember()));

        //then 이런 결과가 나온다.
        assertEquals(CHANGE_IMPOSSIBLE_PINNED_ANSWER, budException.getErrorCode());
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 게시물 리스트 검색")
    void success_searchPosts() {
        //given
        List<PostDto> posts = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            posts.add(PostDto.builder()
                    .id(i)
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
                    .isLike(true)
                    .isFollow(true)
                    .isScrap(true)
                    .build());
        }

        List<Image> images = getImageList();

        PageRequest pageRequest = PageRequest.of(0, 3);

        given(postRepositoryQuerydsl.findAllByPostStatus(anyLong(),anyString(), any(),
                any(), any(), any()))
                .willReturn(new PageImpl<>(posts, pageRequest, 3));

        given(imageRepository.findAllByPostId(anyLong()))
                .willReturn(images);

        //when
        Page<SearchPost.Response> responses = postService.searchPosts(
                Member.builder().id(1L).build(), "제목", HIT, ASC,
                0, 3, FEED);

        //then
        assertEquals(3, responses.getContent().size());
        assertEquals(1L, responses.getContent().get(0).getId());
        assertEquals("제목1", responses.getContent().get(0).getTitle());
        assertEquals(Arrays.toString(new String[]{"img0", "img1", "img2"}),
                Arrays.toString(responses.getContent().get(2).getImageUrls()));
        assertEquals("내용1", responses.getContent().get(0).getContent());
        assertEquals(1, responses.getContent().get(0).getHitCount());
        assertEquals(1, responses.getContent().get(0).getCommentCount());
        assertEquals(1, responses.getContent().get(0).getScrapCount());
        assertEquals(ACTIVE, responses.getContent().get(0).getPostStatus());
        assertEquals(FEED, responses.getContent().get(0).getPostType());
        assertTrue(responses.getContent().get(0).isLike());
        assertTrue(responses.getContent().get(0).isScrap());
        assertTrue(responses.getContent().get(0).isFollow());
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 게시물 상세 데이터 검색")
    void success_searchPost() {
        //given
        PostDto postDto = getPostDto();

        given(postRepositoryQuerydsl.findByPostId(any(), anyLong()))
                .willReturn(Optional.of(postDto));

        given(imageRepository.findAllByPostId(anyLong()))
                .willReturn(getImageList());

        //when
        SearchPost.Response response = postService.searchPost(Member.builder()
                        .id(1L)
                        .build(),
                1L);

        //then
        assertEquals(1L, response.getId());
        assertEquals("title", response.getTitle());
        assertEquals(Arrays.toString(new String[]{"img0", "img1", "img2"}),
                Arrays.toString(response.getImageUrls()));
        assertEquals("content", response.getContent());
        assertEquals(ACTIVE, response.getPostStatus());
        assertEquals(FEED, response.getPostType());
        assertTrue(response.isLike());
        assertTrue(response.isScrap());
        assertTrue(response.isFollow());
    }

    @Test
    @WithMockUser
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
    @WithMockUser
    @DisplayName("실패 - 게시물 상세 데이터 검색")
    void fail_searchPost() {
        //given
        given(postRepositoryQuerydsl.findByPostId(any(), anyLong()))
                .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
                () -> postService.searchPost(Member.builder().id(1L).build(), 1L));

        //then
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @WithMockUser
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
    @WithMockUser
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
        boolean isAdd = postService.addLike(post.getId(), member);

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
        boolean isAdd = postService.addLike(post.getId(), member);

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
                () -> postService.addLike((long)1, new Member()));

        //then
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("성공 - 게시글 스크랩 추가")
    void success_addPostScrap(){
        //given
        Post post = getPost();
        post.setId((long)1);
        post.setScrapCount(2);

        Member member = Member.builder()
                .id(1L)
                .status(MemberStatus.VERIFIED)
                .userId("xoals25")
                .profileImg("abcde.jpg")
                .nickname("엄탱")
                .job("백")
                .oAuthAccessToken("token")
                .build();

        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));

        given(scrapRepository.findByPostIdAndMemberId(member.getId(), 1L))
                .willReturn(Optional.empty());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        boolean isAdd = scrapService.addScrap(post.getId(), member);

        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        assertEquals(1L, post.getId());
        assertEquals(3, postCaptor.getValue().getScrapCount());
        assertTrue(isAdd);
    }

    @Test
    @DisplayName("성공 - 게시물 스크랩 해제")
    void success_postScrapRemove(){
        //given
        Post post = getPost();
        post.setId((long)1);
        post.setScrapCount(2);

        Member member = Member.builder()
                .id(1L)
                .status(MemberStatus.VERIFIED)
                .userId("xoals25")
                .profileImg("abcde.jpg")
                .nickname("엄탱")
                .job("백")
                .oAuthAccessToken("token")
                .build();

        Scrap scrap = Scrap.builder()
                .post(post)
                .member(member)
                .build();

        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));

        given(scrapRepository.findByPostIdAndMemberId(anyLong(), anyLong()))
                .willReturn(Optional.of(scrap));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        //when
        boolean isAdd = scrapService.addScrap(post.getId(), member);

        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        assertEquals(1L, post.getId());
        assertEquals(1, postCaptor.getValue().getScrapCount());
        assertFalse(isAdd);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 게시글 스크랩 ")
    void fail_postScrap() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
                () -> scrapService.addScrap(1L, Member.builder().id(1L).build()));

        //then
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    void success_searchMyPagePosts(){
        //given
        List<PostDto> posts = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            posts.add(PostDto.builder()
                    .id(i)
                    .title("제목")
                    .member(Member.builder()
                            .id(1L)
                            .build())
                    .content("내용")
                    .commentCount(i)
                    .likeCount(i)
                    .scrapCount(i)
                    .hitCount(i)
                    .postStatus(i == 3 ? INACTIVE : ACTIVE)
                    .postType(FEED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isLike(true)
                    .isFollow(true)
                    .isScrap(true)
                    .build());
        }

        List<Image> images = getImageList();

        given(imageRepository.findAllByPostId(anyLong()))
                .willReturn(images);

        given(postRepositoryQuerydsl.findAllByMyPagePost(anyLong(), anyLong(), any(), any()))
                .willReturn(new PageImpl<>(posts, PageRequest.of(0, 3), 3));

        //when
        Page<SearchMyPagePost.Response> searchMyPagePosts =
                postService.searchMyPagePosts(Member.builder()
                                .id(1L)
                                .build(),
                        1L,
                        FEED,
                        PageRequest.of(0, 3)
                );

        //then
        assertEquals(3, searchMyPagePosts.getContent().size());
        assertEquals(1, searchMyPagePosts.getContent().get(0).getPostId());
        assertEquals("제목", searchMyPagePosts.getContent().get(0).getTitle());
        assertEquals(1, searchMyPagePosts.getContent().get(0).getPostRegisterMemberId());
        assertEquals(Arrays.toString(new String[]{"img0", "img1", "img2"}),
                Arrays.toString(searchMyPagePosts.getContent().get(2).getImageUrls()));
        assertEquals("내용", searchMyPagePosts.getContent().get(0).getContent());
        assertEquals(1, searchMyPagePosts.getContent().get(0).getCommentCount());
        assertEquals(1, searchMyPagePosts.getContent().get(0).getLikeCount());
        assertEquals(1, searchMyPagePosts.getContent().get(0).getScrapCount());
        assertEquals(1, searchMyPagePosts.getContent().get(0).getHitCount());
        assertEquals(ACTIVE, searchMyPagePosts.getContent().get(0).getPostStatus());
        assertEquals(FEED, searchMyPagePosts.getContent().get(0).getPostType());
        assertTrue(searchMyPagePosts.getContent().get(0).isLike());
        assertTrue(searchMyPagePosts.getContent().get(0).isScrap());
        assertTrue(searchMyPagePosts.getContent().get(0).isFollow());
    }
    private QnaAnswerPin getQnaAnswerPin() {
        return QnaAnswerPin.builder()
                .post(getPost())
                .qnaAnswer(getQnaAnswer())
                .build();
    }

    private static QnaAnswer getQnaAnswer() {
        return QnaAnswer.builder()
                .id(1L)
                .member(getMember())
                .post(getPost())
                .content("content")
                .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                .build();
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

    private static PostDto getPostDto() {
        return PostDto.builder()
                .id(1L)
                .member(getMember())
                .title("title")
                .content("content")
                .postStatus(ACTIVE)
                .postType(PostType.FEED)
                .isScrap(true)
                .isFollow(true)
                .isLike(true)
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

    private static List<Image> getImageList() {
        List<Image> images = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            images.add(Image.builder()
                    .id((long) (i + 1))
                    .imagePath("img" + i)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        return images;
    }

    private static String[] getImageUrlList(int size) {
        String[] images = new String[size];

        for (int i = 0; i < size; i++) {
            images[i] = "img" + i;
        }

        return images;
    }
}