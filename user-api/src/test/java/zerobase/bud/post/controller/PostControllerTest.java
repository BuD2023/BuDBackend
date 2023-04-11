package zerobase.bud.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.post.service.PostService;
import zerobase.bud.security.TokenProvider;

@WebMvcTest(PostController.class)
@AutoConfigureRestDocs
class PostControllerTest {

    @MockBean
    private PostService postService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockUser
    void success_createPost() throws Exception {
        //given
        Map<String, String> input = new HashMap<>();
        input.put("postType", "QNA");
        input.put("title", "게시글 제목 테스트");
        input.put("content", "게시글 본문 테스트");

        List<MockMultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("multipartFile",
            "health.jpg",
            "image/jpg",
            "<<jpeg data>>".getBytes()));

        String contents = objectMapper.writeValueAsString(input);

        given(tokenProvider.getUserId(anyString()))
            .willReturn("value");

        given(postService.createPost(anyString(), any(), any()))
            .willReturn("success");
        //when
        //then
        mockMvc.perform(multipart("/posts")
                .file(images.get(0))
                .file(new MockMultipartFile("createPostRequest", "",
                    "application/json", contents.getBytes(
                    StandardCharsets.UTF_8)))
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );

    }

    @Test
    @WithMockUser
    void success_updatePost() throws Exception {
        //given
        Map<String, String> input = new HashMap<>();
        input.put("postId", "1");
        input.put("postType", "QNA");
        input.put("title", "수정 테스트");
        input.put("content", "게시글 본문 수정 테스트");

        List<MockMultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("multipartFile",
            "swimming.jpg",
            "image/jpg",
            "<<jpeg data>>".getBytes()));

        String contents = objectMapper.writeValueAsString(input);

        given(tokenProvider.getUserId(anyString()))
            .willReturn("value");

        given(postService.updatePost(any(), any()))
            .willReturn("success");
        //when
        //then
        mockMvc.perform(multipart("/posts/update")
                .file(images.get(0))
                .file(new MockMultipartFile("updatePostRequest", "",
                    "application/json", contents.getBytes(
                    StandardCharsets.UTF_8)))
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );

    }
}