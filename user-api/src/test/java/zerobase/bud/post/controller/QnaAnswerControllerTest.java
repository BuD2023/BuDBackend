package zerobase.bud.post.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import zerobase.bud.comment.service.QnaAnswerCommentService;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.post.dto.*;
import zerobase.bud.post.service.QnaAnswerService;
import zerobase.bud.post.type.QnaAnswerStatus;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(QnaAnswerController.class)
@AutoConfigureRestDocs
class QnaAnswerControllerTest {

    @MockBean
    private QnaAnswerService qnaAnswerService;

    @MockBean
    private QnaAnswerCommentService qnaAnswerCommentService;

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

    @BeforeEach
    void init(WebApplicationContext context, RestDocumentationContextProvider contextProvider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(contextProvider))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }


    @Test
    @WithMockUser
    void success_createQnaAnswer() throws Exception {
        //given
        Map<String, String> input = new HashMap<>();
        input.put("postId", "1");
        input.put("content", "답변 본문 테스트");

        List<MockMultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("multipartFile",
            "health.jpg",
            "image/jpg",
            "<<jpeg data>>".getBytes()));

        String contents = objectMapper.writeValueAsString(input);

        given(qnaAnswerService.createQnaAnswer(any(), any(), any()))
                .willReturn("finish");

        //when
        //then
        mockMvc.perform(multipart("/posts/qna-answers")
                        .file(images.get(0))
                        .file(new MockMultipartFile("createQnaAnswerRequest", "",
                            "application/json", contents.getBytes(
                            StandardCharsets.UTF_8)))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("finish"))
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
        Map<String, String> input = new HashMap<>();
        input.put("content", "답변 업데이트 본문 테스트");

        List<MockMultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("multipartFile",
            "health.jpg",
            "image/jpg",
            "<<jpeg data>>".getBytes()));

        String contents = objectMapper.writeValueAsString(input);

        given(qnaAnswerService.updateQnaAnswer(anyLong(), any(), any(), any()))
                .willReturn(3L);

        //when
        //then
        mockMvc.perform(multipart("/posts/qna-answers/1")
                        .file(images.get(0))
                        .file(new MockMultipartFile("updateQnaAnswerRequest", "",
                            "application/json", contents.getBytes(
                            StandardCharsets.UTF_8)))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").value(3L))
                        .andDo(
                                document("{class-name}/{method-name}",
                                        preprocessRequest(prettyPrint()),
                                        preprocessResponse(prettyPrint()))
                        );
    }

    @Test
    @WithMockUser
    @DisplayName("qna 댓글 좋아요 성공")
    void successCommentLike() throws Exception {
        //given
        given(qnaAnswerCommentService.commentLike(anyLong(), any()))
                .willReturn(1L);
        //when
        //then
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/posts/qna-answers/qna-comments/{qnaCommentId}/like", 1)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );

    }

    @Test
    @WithMockUser
    @DisplayName("qna 댓글 핀 성공")
    void successCommentPin() throws Exception {
        //given
        given(qnaAnswerCommentService.commentPin(anyLong(), any()))
                .willReturn(1L);
        //when
        //then
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/posts/qna-answers/qna-comments/{qnaCommentId}/pin", 1)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }

    @Test
    @WithMockUser
    @DisplayName("qna 게시물에 댓글 핀 삭제 성공")
    void successCancelCommentPin() throws Exception {
        //given
        given(qnaAnswerCommentService.cancelCommentPin(anyLong(), any()))
                .willReturn(1L);
        //when
        //then
        this.mockMvc.perform(delete("/posts/qna-answers/{qnaAnswerId}/qna-comments/pin", 1)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }

    @Test
    @WithMockUser
    @DisplayName("qna 게시물 댓글 가져오기 성공")
    void successComments() throws Exception {
        List<QnaAnswerCommentDto> recomments = List.of(
                QnaAnswerCommentDto.builder()
                        .commentId(2L)
                        .content("이것은 댓글입니다")
                        .numberOfLikes(0)
                        .memberName("아디다스")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(2L)
                        .createdAt("0 초전")
                        .isReader(false)
                        .isReaderLiked(false)
                        .build(),
                QnaAnswerCommentDto.builder()
                        .commentId(3L)
                        .content("mystacksmytreasure")
                        .numberOfLikes(3)
                        .memberName("먼지")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(2L)
                        .createdAt("0 초전")
                        .isReader(false)
                        .isReaderLiked(false)
                        .build(),
                QnaAnswerCommentDto.builder()
                        .commentId(2L)
                        .content("이것은 대댓글입니다")
                        .numberOfLikes(3)
                        .memberName("캉골정품")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(3L)
                        .createdAt("0 초전")
                        .isReader(false)
                        .isReaderLiked(true)
                        .build()
        );

        List<QnaAnswerCommentDto> dtos = List.of(
                QnaAnswerCommentDto.builder()
                        .commentId(1L)
                        .content("지금터미널임좀기달")
                        .numberOfLikes(0)
                        .memberName("갈팡질팡")
                        .numberOfComments(recomments.size())
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(2L)
                        .createdAt("0 초전")
                        .isReader(true)
                        .isPinned(true)
                        .isReaderLiked(false)
                        .reComments(recomments)
                        .build(),
                QnaAnswerCommentDto.builder()
                        .commentId(2L)
                        .content("구제바지줄여가지고구바")
                        .numberOfLikes(3)
                        .memberName("멀라")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(4L)
                        .createdAt("0 초전")
                        .isReader(true)
                        .numberOfComments(0)
                        .isReaderLiked(false)
                        .isPinned(false)
                        .build(),
                QnaAnswerCommentDto.builder()
                        .commentId(2L)
                        .numberOfComments(0)
                        .content("어떤건보풀하나도없더라공,,ㅋ")
                        .numberOfLikes(3)
                        .memberName("쓰레기")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(4L)
                        .createdAt("0 초전")
                        .isReader(true)
                        .isPinned(false)
                        .isReaderLiked(false)
                        .build()

        );


        given(qnaAnswerCommentService.comments(anyLong(), any(), anyInt(), anyInt()))
                .willReturn(new SliceImpl<>(dtos));


        this.mockMvc.perform(get("/posts/qna-answers/{qnaAnswerId}/qna-comments", 1)
                        .param("page", "0")
                        .param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].commentId").type(JsonFieldType.NUMBER)
                                                .description("댓글의 id"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING)
                                                .description("댓글 내용"),
                                        fieldWithPath("content[].numberOfComments").type(JsonFieldType.NUMBER)
                                                .description("댓글 수"),
                                        fieldWithPath("content[].numberOfLikes").type(JsonFieldType.NUMBER)
                                                .description("좋아요 수"),
                                        fieldWithPath("content[].memberName").type(JsonFieldType.STRING)
                                                .description("작성자 닉네임"),
                                        fieldWithPath("content[].memberProfileUrl").type(JsonFieldType.STRING)
                                                .description("작성자 프로필 이미지"),
                                        fieldWithPath("content[].memberId").type(JsonFieldType.NUMBER)
                                                .description("작성자 아이디"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING)
                                                .description("댓글 단 시간"),
                                        fieldWithPath("content[].isPinned").type(JsonFieldType.BOOLEAN).optional()
                                                .description("핀 된 댓글인지"),
                                        fieldWithPath("content[].isReader").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 사람인지"),
                                        fieldWithPath("content[].isReaderLiked").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 사람이 좋아요를 누른 댓글인지"),
                                        fieldWithPath("content[].reComments").type(JsonFieldType.ARRAY).optional()
                                                .description("대댓글"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("number").type(JsonFieldType.NUMBER)
                                                .description("현재 몇번째 페이지인지"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("하나의 페이지 안에 몇개의 채팅룸이 들어갔는지")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("qna 댓글 삭제 성공")
    void successDeleteComment() throws Exception {
        //given
        given(qnaAnswerCommentService.delete(anyLong(), any()))
                .willReturn(2L);
        //when
        //then
        this.mockMvc.perform(delete("/posts/qna-answers/qna-comments/{qnaCommentId}", 1)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }

    @Test
    @WithMockUser
    void success_qnaAnswerPin() throws Exception {
        //given
        given(qnaAnswerService.qnaAnswerPin(anyLong(), any()))
            .willReturn(3L);

        //when
        //then
        mockMvc.perform(post("/posts/qna-answers/3/pin")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(3))
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );
    }

    @Test
    @WithMockUser
    void success_cancelQnaAnswerPin() throws Exception {
        //given
        given(qnaAnswerService.cancelQnaAnswerPin(anyLong(), any()))
            .willReturn(1L);

        //when
        //then
        mockMvc.perform(delete("/posts/qna-answers/pin/1")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(1))
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
                    .isLike(true)
                    .isFollow(false)
                    .build());
        }

        given(qnaAnswerService.searchQnaAnswers(any(), anyLong(), any()))
                .willReturn(new PageImpl<>(list, PageRequest.of(0, 3), 10));

        //when
        //then
        mockMvc.perform(get("/posts/qna-answers")
                        .param("postId", "1")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "DATE,DESC")
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
                .andExpect(jsonPath("content[0].like").value(true))
                .andExpect(jsonPath("content[0].follow").value(false))
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
                                        fieldWithPath("content[].like").type(
                                                        JsonFieldType.BOOLEAN)
                                                .description("qna 답글에 좋아요 클릭 유무"),
                                        fieldWithPath("content[].follow").type(
                                                        JsonFieldType.BOOLEAN)
                                                .description("qna 답글 작성자 팔로우 유무"),
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
        mockMvc.perform(delete("/posts/qna-answers/2")
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

    @Test
    @WithMockUser
    @DisplayName("성공 - QNA Answer 좋아요")
    void success_addQnaAnswerLike() throws Exception {
        //given
        given(qnaAnswerService.addLike(anyLong(), any()))
                .willReturn(true);
        //when
        //then
        mockMvc.perform(post("/posts/qna-answers/1/like")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$").value("좋아요"))
                .andExpect(status().isOk())
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - QNA Answer 좋아요 해제")
    void success_removeQnaAnswerLike() throws Exception {
        //given
        given(qnaAnswerService.addLike(anyLong(), any()))
                .willReturn(false);
        //when
        //then
        mockMvc.perform(post("/posts/qna-answers/1/like")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$").value("좋아요 해제"))
                .andExpect(status().isOk())
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 생성 성공")
    void successCreateComment() throws Exception {
        //given
        given(qnaAnswerCommentService.createComment(anyLong(), any(), anyString()))
                .willReturn(
                        QnaAnswerCommentDto.builder()
                                .commentId(2L)
                                .content("이것은 댓글입니다")
                                .numberOfLikes(0)
                                .memberName("아디다스")
                                .memberProfileUrl("profiles/images.jpg")
                                .memberId(2L)
                                .createdAt("0 초전")
                                .build()
                );
        //when
        //then
        this.mockMvc.perform(post("/posts/qna-answers/{postId}/qna-comments", 23)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CreateComment.Request.builder()
                                        .content("이것은 댓글입니다").build()
                        ))
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );

    }

    @Test
    @WithMockUser
    @DisplayName("댓글 수정 성공")
    void successModifyComment() throws Exception {
        //given
        given(qnaAnswerCommentService.modifyComment(anyLong(), any(), anyString()))
                .willReturn(
                        QnaAnswerCommentDto.builder()
                                .commentId(2L)
                                .content("이것은 댓글입니다")
                                .numberOfLikes(0)
                                .memberName("아디다스")
                                .memberProfileUrl("profiles/images.jpg")
                                .memberId(2L)
                                .createdAt("0 초전")
                                .build()
                );
        //when
        //then
        this.mockMvc.perform(put("/posts/qna-answers/qna-comments/{commentId}/modify", 3)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CreateComment.Request.builder()
                                        .content("이것은 댓글입니다").build()
                        ))
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );

    }

    @Test
    @WithMockUser
    @DisplayName("대댓글 생성 성공")
    void successCreateRecomment() throws Exception {
        //given
        given(qnaAnswerCommentService.createRecomment(anyLong(), any(), anyString()))
                .willReturn(
                        QnaAnswerRecommentDto.builder()
                                .commentId(2L)
                                .content("이것은 댓글입니다")
                                .numberOfLikes(0)
                                .memberName("아디다스")
                                .memberProfileUrl("profiles/images.jpg")
                                .memberId(2L)
                                .createdAt("0 초전")
                                .build()
                );
        //when
        //then
        this.mockMvc.perform(post("/posts/qna-answers/qna-comments/{commentId}", 23)
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CreateComment.Request.builder()
                                        .content("이것은 댓글입니다").build()
                        ))
                )
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );

    }
}