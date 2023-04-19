package zerobase.bud.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import zerobase.bud.comment.service.CommentService;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.post.dto.CommentDto;
import zerobase.bud.post.dto.PostDto;
import zerobase.bud.post.service.PostService;
import zerobase.bud.post.service.ScrapService;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(PostController.class)
@AutoConfigureRestDocs
class PostControllerTest {

    @MockBean
    private PostService postService;

    @MockBean
    private ScrapService scrapService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Value("${develop.server.scheme}")
    private String scheme;

    @Value("${develop.server.host}")
    private String host;

    @Value("${develop.server.port}")
    private int port;

    private static final String TOKEN = "Bearer token";

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

        given(postService.createPost(any(), any(), any()))
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

        given(postService.updatePost(anyLong(), any(), any(), any()))
                .willReturn("success");
        //when
        //then
        mockMvc.perform(multipart("/posts/1")
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

    @Test
    @WithMockUser
    @DisplayName("성공 - 전체 게시글 검색")
    void success_searchPosts() throws Exception {
        //given
        List<PostDto> list = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            list.add(PostDto.builder()
                    .id(i)
                    .title("제목" + i)
                    .imageUrls(new String[]{"url1", "url2"})
                    .content("내용" + i)
                    .commentCount(i)
                    .likeCount(i)
                    .scrapCount(i)
                    .hitCount(i)
                    .postStatus(
                            i % 4 == 0 ? PostStatus.INACTIVE : PostStatus.ACTIVE)
                    .postType(PostType.FEED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        given(postService.searchPosts(anyString(), any(), any(), anyInt(),
                anyInt()))
                .willReturn(new PageImpl<>(list));

        //when
        //then
        mockMvc.perform(get("/posts")
                        .param("keyword", "제목")
                        .param("sort", "HIT")
                        .param("order", "ASC")
                        .param("page", "0")
                        .param("size", "3")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].title").value("제목1"))
                .andExpect(jsonPath("content[0].content").value("내용1"))
                .andExpect(jsonPath("content[0].commentCount").value("1"))
                .andExpect(jsonPath("content[0].scrapCount").value("1"))
                .andExpect(jsonPath("content[0].hitCount").value("1"))
                .andExpect(jsonPath("content[0].postStatus").value("ACTIVE"))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(
                                        modifyUris().scheme(scheme).host(host).port(port),
                                        prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].id").type(JsonFieldType.NUMBER)
                                                .description("게시물 id"),
                                        fieldWithPath("content[].title").type(
                                                        JsonFieldType.STRING)
                                                .description("게시물 제목"),
                                        fieldWithPath("content[].imageUrls").type(
                                                        JsonFieldType.ARRAY)
                                                .description("게시물 이미지 링크들"),
                                        fieldWithPath("content[].content").type(
                                                        JsonFieldType.STRING)
                                                .description("게시물 본문"),
                                        fieldWithPath("content[].commentCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("댓글 수"),
                                        fieldWithPath("content[].likeCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("좋아요 수"),
                                        fieldWithPath("content[].scrapCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("스크랩 수"),
                                        fieldWithPath("content[].hitCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("죄회수"),
                                        fieldWithPath("content[].postStatus").type(
                                                        JsonFieldType.STRING)
                                                .description("게시물 상태(게시, 삭제 등)"),
                                        fieldWithPath("content[].postType").type(
                                                        JsonFieldType.STRING)
                                                .description("게시물 종류(FEED, QNA)"),
                                        fieldWithPath("content[].createdAt").type(
                                                        JsonFieldType.STRING)
                                                .description("게시물 등록일"),
                                        fieldWithPath("content[].updatedAt").type(
                                                        JsonFieldType.STRING)
                                                .description("게시물 업데이트일"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("totalElements").type(
                                                        JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 개수"),
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
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 게시글 상세 정보 검색")
    void success_searchPost() throws Exception {
        //given
        PostDto postDto = PostDto.builder()
                .id(1)
                .title("제목")
                .imageUrls(new String[]{"url1", "url2"})
                .content("내용")
                .commentCount(1)
                .likeCount(1)
                .scrapCount(1)
                .hitCount(1)
                .postStatus(PostStatus.ACTIVE)
                .postType(PostType.FEED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.searchPost(anyLong()))
                .willReturn(postDto);
        //when
        //then

        mockMvc.perform(get("/posts/1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("제목"))
                .andExpect(jsonPath("content").value("내용"))
                .andExpect(jsonPath("commentCount").value("1"))
                .andExpect(jsonPath("scrapCount").value("1"))
                .andExpect(jsonPath("hitCount").value("1"))
                .andExpect(jsonPath("postStatus").value("ACTIVE"))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(
                                        modifyUris().scheme(scheme).host(host).port(port),
                                        prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                                .description("게시물 id"),
                                        fieldWithPath("title").type(JsonFieldType.STRING)
                                                .description("게시물 제목"),
                                        fieldWithPath("imageUrls").type(JsonFieldType.ARRAY)
                                                .description("게시물 이미지 링크들"),
                                        fieldWithPath("content").type(JsonFieldType.STRING)
                                                .description("게시물 본문"),
                                        fieldWithPath("commentCount").type(JsonFieldType.NUMBER)
                                                .description("댓글 수"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER)
                                                .description("좋아요 수"),
                                        fieldWithPath("scrapCount").type(JsonFieldType.NUMBER)
                                                .description("스크랩 수"),
                                        fieldWithPath("hitCount").type(JsonFieldType.NUMBER)
                                                .description("죄회수"),
                                        fieldWithPath("postStatus").type(JsonFieldType.STRING)
                                                .description("게시물 상태(게시, 삭제 등)"),
                                        fieldWithPath("postType").type(JsonFieldType.STRING)
                                                .description("게시물 종류(FEED, QNA)"),
                                        fieldWithPath("createdAt").type(JsonFieldType.STRING)
                                                .description("게시물 등록일"),
                                        fieldWithPath("updatedAt").type(JsonFieldType.STRING)
                                                .description("게시물 업데이트일")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 게시글 삭제")
    void success_deletePost() throws Exception {
        //given
        PostDto postDto = PostDto.builder()
                .id(1)
                .title("제목")
                .imageUrls(new String[]{"url1", "url2"})
                .content("내용")
                .commentCount(1)
                .likeCount(1)
                .scrapCount(1)
                .hitCount(1)
                .postStatus(PostStatus.INACTIVE)
                .postType(PostType.FEED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.deletePost(anyLong()))
                .willReturn(postDto.getId());
        //when
        //then

        mockMvc.perform(delete("/posts/1")
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
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 게시글 좋아요")
    void success_addPostLike() throws Exception {
        //given
        given(postService.isLike(anyLong(), any()))
                .willReturn(true);
        //when
        //then
        mockMvc.perform(post("/posts/1/like")
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
    @DisplayName("성공 - 게시글 좋아요 해제")
    void success_removePostLike() throws Exception {
        //given
        given(postService.isLike(anyLong(), any()))
                .willReturn(false);
        //when
        //then
        mockMvc.perform(post("/posts/1/like")
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
    @DisplayName("성공 - 게시글 스크랩 추가")
    void success_addPostScrap() throws Exception {
        //given
        given(scrapService.isScrap(anyLong(), any()))
                .willReturn(true);
        //when
        //then
        mockMvc.perform(post("/posts/1/scrap")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("스크랩 추가"))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 게시글 스크랩 추가")
    void success_removePostScrap() throws Exception {
        //given
        given(scrapService.isScrap(anyLong(), any()))
                .willReturn(false);
        //when
        //then
        mockMvc.perform(post("/posts/1/scrap")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(jsonPath("$").value("스크랩 해제"))
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
    @DisplayName("댓글 좋아요 성공")
    void successCommentLike() throws Exception {
        //given
        given(commentService.commentLike(anyLong(), any()))
                .willReturn(1L);
        //when
        //then
        this.mockMvc.perform(post("/posts/comments/{commentId}/like", 1)
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
    @DisplayName("댓글 핀 성공")
    void successCommentPin() throws Exception {
        //given
        given(commentService.commentPin(anyLong(), any()))
                .willReturn(1L);
        //when
        //then
        this.mockMvc.perform(post("/posts/comments/{commentId}/pin", 1)
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
    @DisplayName("게시물에 댓글 핀 삭제 성공")
    void successCancelCommentPin() throws Exception {
        //given
        given(commentService.cancelCommentPin(anyLong(), any()))
                .willReturn(1L);
        //when
        //then
        this.mockMvc.perform(delete("/posts/{postId}/comments/pin", 1)
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
    @DisplayName("게시물 댓글 가져오기 성공")
    void successComments() throws Exception {
        List<CommentDto> recomments = List.of(
                CommentDto.builder()
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
                CommentDto.builder()
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
                CommentDto.builder()
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

        List<CommentDto> dtos = List.of(
                CommentDto.builder()
                        .commentId(1L)
                        .content("지금터미널임좀기달")
                        .numberOfLikes(0)
                        .memberName("갈팡질팡")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(2L)
                        .createdAt("0 초전")
                        .isReader(true)
                        .isPinned(true)
                        .isReaderLiked(false)
                        .reComments(recomments)
                        .build(),
                CommentDto.builder()
                        .commentId(2L)
                        .content("구제바지줄여가지고구바")
                        .numberOfLikes(3)
                        .memberName("멀라")
                        .memberProfileUrl("profiles/images.jpg")
                        .memberId(4L)
                        .createdAt("0 초전")
                        .isReader(true)
                        .isReaderLiked(false)
                        .isPinned(false)
                        .build(),
                CommentDto.builder()
                        .commentId(2L)
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


        given(commentService.comments(anyLong(), any(), anyInt(), anyInt()))
                .willReturn(new SliceImpl<>(dtos));


        this.mockMvc.perform(get("/posts/{postId}/comments",1)
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
    @DisplayName("댓글 삭제 성공")
    void successDeleteComment() throws Exception {
        //given
        given(commentService.delete(anyLong(), any()))
                .willReturn(2L);
        //when
        //then
        this.mockMvc.perform(delete("/posts/comments/{commentId}", 1)
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
}