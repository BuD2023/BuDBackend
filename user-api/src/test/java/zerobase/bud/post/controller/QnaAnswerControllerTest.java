package zerobase.bud.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.post.dto.CreateQnaAnswer;
import zerobase.bud.post.dto.SearchQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.service.QnaAnswerService;
import zerobase.bud.post.type.QnaAnswerStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Value("${develop.server.scheme}")
    private String scheme;

    @Value("${develop.server.host}")
    private String host;

    @Value("${develop.server.port}")
    private int port;

    @Test
    @WithMockUser
    void success_createQnaAnswer() throws Exception {
        //given
        given(qnaAnswerService.createQnaAnswer(any(), any()))
            .willReturn("finish");

        //when
        //then
        mockMvc.perform(post("/posts/qna-answer")
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
        mockMvc.perform(put("/posts/qna-answer")
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

    @Test
    @WithMockUser
    @DisplayName("success search list QnaAnswer")
    void success_searchQnaAnswers() throws Exception{
        //given
        List<SearchQnaAnswer.Response> list = new ArrayList<>();

        for (long i = 1; i <= 3; i++) {
            list.add(SearchQnaAnswer.Response.builder()
                    .id(i)
                    .member(null)
                    .content("내용입니다" + i)
                    .commentCount(i)
                    .likeCount(i)
                    .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                    .isQnaAnswerPin(i == 1)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        given(qnaAnswerService.searchQnaAnswers(anyLong(), any()))
                .willReturn(new PageImpl<>(list, PageRequest.of(0, 3), 10));

        //when
        //then
        mockMvc.perform(get("/posts/qna-answer")
                        .param("postId", "1")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "DATE,DESC")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].content").value("내용입니다1"))
                .andExpect(jsonPath("content[0].commentCount").value(1))
                .andExpect(jsonPath("content[0].likeCount").value(1))
                .andExpect(jsonPath("content[0].qnaAnswerStatus").value(QnaAnswerStatus.ACTIVE.toString()))
                .andExpect(jsonPath("content[0].qnaAnswerPin").value(true))
                .andExpect(jsonPath("content[1].qnaAnswerPin").value(false))
                .andExpect(jsonPath("content[2].qnaAnswerPin").value(false))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(
                                        modifyUris().scheme(scheme).host(host).port(port),
                                        prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].id").type(JsonFieldType.NUMBER)
                                                .description("qna 답글 id"),
                                        fieldWithPath("content[].member").type(JsonFieldType.NULL)
                                                .description("qna 답글 작성한 member 정보"),
                                        fieldWithPath("content[].content").type(
                                                        JsonFieldType.STRING)
                                                .description("qna 답글 본문"),
                                        fieldWithPath("content[].commentCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("qna 답글 댓글 수"),
                                        fieldWithPath("content[].likeCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("qna 답글 좋아요 수"),
                                        fieldWithPath("content[].qnaAnswerStatus").type(
                                                        JsonFieldType.STRING)
                                                .description("qna 답글 상태(게시, 삭제 등)"),
                                        fieldWithPath("content[].qnaAnswerPin").type(
                                                        JsonFieldType.BOOLEAN)
                                                .description("qna 답글이 고정 답글인지 아닌지"),
                                        fieldWithPath("content[].createdAt").type(
                                                        JsonFieldType.STRING)
                                                .description("qna 답글 등록일"),
                                        fieldWithPath("content[].updatedAt").type(
                                                        JsonFieldType.STRING)
                                                .description("qna 답글 업데이트일"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("totalElements").type(
                                                        JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 개수"),
                                        fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 페이지 수"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("요청 데이터 수"),
                                        fieldWithPath("numberOfElements").type(
                                                        JsonFieldType.NUMBER)
                                                .description("현재 페이지에서 보여지는 데이터 수")
                                )
                        ));

    }

    @Test
    @WithMockUser
    @DisplayName("success delete QnaAnswer")
    void success_deleteQnaAnswer() throws Exception{
        //given
        //when
        //then
        mockMvc.perform(get("/posts/qna-answer")
                        .param("postId", "1")
                        .param("page", "0")
                        .param("size", "3")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(
                                        modifyUris().scheme(scheme).host(host).port(port),
                                        prettyPrint()),
                                preprocessResponse(prettyPrint())
                        ));
    }
}