package zerobase.bud.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.post.dto.CreateQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.service.QnaAnswerService;

@WebMvcTest(QnaAnswerController.class)
@AutoConfigureRestDocs
class QnaAnswerControllerTest {

    @MockBean
    private QnaAnswerService qnaAnswerService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockUser
    void success_createQnaAnswer() throws Exception {
        //given
        given(qnaAnswerService.createQnaAnswer(anyString(), any()))
            .willReturn("finish");

        given(tokenProvider.getUserId(anyString()))
            .willReturn(TOKEN);
        //when
        //then
        mockMvc.perform(post("/posts/answer")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    CreateQnaAnswer.Request.builder()
                        .postId(1L)
                        .content("content")
                        .build()
                )).with(csrf()))
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
    void success_updateQnaAnswer() throws Exception {
        //given
        given(qnaAnswerService.updateQnaAnswer(any()))
            .willReturn(3L);

        //when
        //then
        mockMvc.perform(put("/posts/answer")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    UpdateQnaAnswer.Request.builder()
                        .qnaAnswerId(1L)
                        .content("content")
                        .build()
                )).with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );
    }
}