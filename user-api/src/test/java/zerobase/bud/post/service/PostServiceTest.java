package zerobase.bud.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;
import static zerobase.bud.post.type.PostStatus.ACTIVE;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.github.domain.GithubInfo;
import zerobase.bud.github.repository.GithubInfoRepository;
import zerobase.bud.post.domain.Image;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.dto.CreatePost.Request;
import zerobase.bud.post.repository.ImageRepository;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.type.PostType;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private GithubInfoRepository githubInfoRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void success_createPostWithImage() {
        //given 어떤 데이터가 주어졌을 때
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(postRepository.save(any()))
            .willReturn(getPost());

        given(imageRepository.save(any()))
            .willReturn(Image.builder()
                .post(getPost())
                .imageUrl("imageUrl")
                .build());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(
            Image.class);
        //when 어떤 경우에
        String result = postService.createPost("abcd@naver.com",
            CreatePost.Request.builder()
                .title("t")
                .content("c")
                .imageUrl("i")
                .postType(PostType.FEED)
                .build());

        //then 이런 결과가 나온다.
        verify(postRepository, times(1)).save(captor.capture());
        verify(imageRepository, times(1)).save(imageCaptor.capture());
        assertEquals("t", captor.getValue().getTitle());
        assertEquals("c", captor.getValue().getContent());
        assertEquals(ACTIVE, captor.getValue().getPostStatus());
        assertEquals(PostType.FEED, captor.getValue().getPostType());
        assertEquals("i", imageCaptor.getValue().getImageUrl());
        assertEquals("title", imageCaptor.getValue().getPost().getTitle());
        assertEquals("content", imageCaptor.getValue().getPost().getContent());
        assertEquals("abcd@naver.com", result);
    }

    @Test
    void success_createPost() {
        //given 어떤 데이터가 주어졌을 때
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(postRepository.save(any()))
            .willReturn(getPost());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        //when 어떤 경우에
        String result = postService.createPost("abcd@naver.com",
            CreatePost.Request.builder()
                .title("t")
                .content("c")
                .imageUrl("")
                .postType(PostType.QNA)
                .build());

        //then 이런 결과가 나온다.
        verify(postRepository, times(1)).save(captor.capture());
        assertEquals("t", captor.getValue().getTitle());
        assertEquals("c", captor.getValue().getContent());
        assertEquals(ACTIVE, captor.getValue().getPostStatus());
        assertEquals(PostType.QNA, captor.getValue().getPostType());
        assertEquals("abcd@naver.com", result);
    }

    @Test
    @DisplayName("NOT_REGISTERED_MEMBER_createPost")
    void NOT_REGISTERED_MEMBER_createPost() {
        //given 어떤 데이터가 주어졌을 때
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> postService.createPost("abcd@naver.com",
                Request.builder()
                    .title("t")
                    .content("c")
                    .imageUrl("i")
                    .postType(PostType.FEED)
                    .build()));

        //then 이런 결과가 나온다.
        assertEquals(NOT_REGISTERED_MEMBER, budException.getErrorCode());
    }

    private static Post getPost() {
        return Post.builder()
            .member(getGithubInfo().getMember())
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();
    }

    private static GithubInfo getGithubInfo() {
        return GithubInfo.builder()
            .id(1L)
            .accessToken("accessToken")
            .email("abcd@naver.com")
            .username("userName")
            .build();
    }

}