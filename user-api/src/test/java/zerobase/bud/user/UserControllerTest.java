package zerobase.bud.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.notification.dto.NotificationInfoDto;
import zerobase.bud.notification.service.NotificationInfoService;
import zerobase.bud.post.dto.SearchMyPagePost;
import zerobase.bud.post.dto.SearchScrap;
import zerobase.bud.post.service.PostService;
import zerobase.bud.post.service.ScrapService;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;
import zerobase.bud.user.controller.UserController;
import zerobase.bud.user.dto.FollowDto;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private ScrapService scrapService;

    @MockBean
    private PostService postService;

    @MockBean
    private NotificationInfoService notificationInfoService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${develop.server.scheme}")
    private String scheme;

    @Value("${develop.server.host}")
    private String host;

    @Value("${develop.server.port}")
    private int port;

    private static String token = "임의의accesstoken";

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
    @DisplayName("유저 팔로우/팔로우 취소 성공")
    void successFollowTest() throws Exception {
        //given
        given(userService.follow(anyLong(), any())).willReturn(1L);
        //when
        //then
        this.mockMvc.perform(post("/users/{userId}/follows", 1L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }

    @Test
    @WithMockUser
    @DisplayName("회원 프로필 조회 성공")
    void successReadProfileTest() throws Exception {
        //given
        given(userService.readProfile(anyLong(), any())).willReturn(
                UserDto.builder().userId("thdefn")
                        .description("암생각없다")
                        .id(1L)
                        .isFollowing(true)
                        .isReader(false)
                        .nickName("닉넴")
                        .level(1L)
                        .profileUrl("ahd.jpg")
                        .numberOfFollows(3L)
                        .numberOfFollowers(4L)
                        .numberOfPosts(20L)
                        .build()
        );
        //when
        //then
        this.mockMvc.perform(get("/users/{userId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("userId").type(JsonFieldType.STRING)
                                                .description("깃허브 유저 아이디"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                                .description("회원 고유값"),
                                        fieldWithPath("description").type(JsonFieldType.STRING)
                                                .description("한줄 소개"),
                                        fieldWithPath("numberOfFollows").type(JsonFieldType.NUMBER)
                                                .description("회원이 팔로우 하는 사람 수"),
                                        fieldWithPath("numberOfFollows").type(JsonFieldType.NUMBER)
                                                .description("회원이 팔로우 하는 사람 수"),
                                        fieldWithPath("numberOfPosts").type(JsonFieldType.NUMBER)
                                                .description("회원이 쓴 글의 개수"),
                                        fieldWithPath("nickName").type(JsonFieldType.STRING)
                                                .description("회원의 닉네임"),
                                        fieldWithPath("level").type(JsonFieldType.NUMBER)
                                                .description("회원의 레벨"),
                                        fieldWithPath("isFollowing").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 유저가 이 회원을 팔로잉하고 있는지"),
                                        fieldWithPath("isReader").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 유저가 이 프로필 회원인지"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("나의 프로필 조회 성공")
    void successReadMyProfileTest() throws Exception {
        //given
        given(userService.readMyProfile(any())).willReturn(
                UserDto.builder().userId("thdefn")
                        .description("암생각없다")
                        .id(1L)
                        .nickName("닉넴")
                        .level(1L)
                        .profileUrl("ahd.jpg")
                        .numberOfFollows(3L)
                        .numberOfFollowers(4L)
                        .numberOfPosts(20L)
                        .build()
        );
        //when
        //then
        this.mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("userId").type(JsonFieldType.STRING)
                                                .description("깃허브 유저 아이디"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER)
                                                .description("회원 고유값"),
                                        fieldWithPath("description").type(JsonFieldType.STRING)
                                                .description("한줄 소개"),
                                        fieldWithPath("numberOfFollows").type(JsonFieldType.NUMBER)
                                                .description("회원이 팔로우 하는 사람 수"),
                                        fieldWithPath("numberOfFollows").type(JsonFieldType.NUMBER)
                                                .description("회원이 팔로우 하는 사람 수"),
                                        fieldWithPath("numberOfPosts").type(JsonFieldType.NUMBER)
                                                .description("회원이 쓴 글의 개수"),
                                        fieldWithPath("nickName").type(JsonFieldType.STRING)
                                                .description("회원의 닉네임"),
                                        fieldWithPath("level").type(JsonFieldType.NUMBER)
                                                .description("회원의 레벨"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("유저의 팔로우 리스트 조회 성공")
    void successReadMyFollowingsTest() throws Exception {
        //given
        List<FollowDto> dtos = List.of(
                FollowDto.builder()
                        .id(1L)
                        .userId("haden")
                        .nickName("닉넴")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(true)
                        .build(),
                FollowDto.builder()
                        .id(2L)
                        .userId("thddd")
                        .nickName("사과")
                        .description("안녕하세요 ~~~ 저는")
                        .profileUrl("affd.jpg")
                        .isFollowing(true)
                        .build(),
                FollowDto.builder()
                        .id(3L)
                        .userId("agvdg")
                        .nickName("알수없음")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(true)
                        .build()
        );

        given(userService.readMyFollowings(any())).willReturn(dtos);
        //when
        //then
        this.mockMvc.perform(get("/users/follows")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("[].userId").type(JsonFieldType.STRING)
                                                .description("깃허브 유저 아이디"),
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER)
                                                .description("회원 고유값"),
                                        fieldWithPath("[].description").type(JsonFieldType.STRING)
                                                .description("한줄 소개"),
                                        fieldWithPath("[].nickName").type(JsonFieldType.STRING)
                                                .description("회원의 닉네임"),
                                        fieldWithPath("[].profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url"),
                                        fieldWithPath("[].isFollowing").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 회원이 팔로잉하고 있는 사람인지")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("회원 팔로우 리스트 조회 성공")
    void successReadFollowingsTest() throws Exception {
        //given
        List<FollowDto> dtos = List.of(
                FollowDto.builder()
                        .id(1L)
                        .userId("haden")
                        .nickName("닉넴")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .isReader(false)
                        .build(),
                FollowDto.builder()
                        .id(2L)
                        .userId("thddd")
                        .nickName("사과")
                        .description("안녕하세요 ~~~ 저는")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .isReader(true)
                        .build(),
                FollowDto.builder()
                        .id(3L)
                        .userId("agvdg")
                        .nickName("알수없음")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .isReader(false)
                        .build()
        );

        given(userService.readFollowings(anyLong(), any())).willReturn(dtos);
        //when
        //then
        this.mockMvc.perform(get("/users/{userId}/follows",1L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("[].userId").type(JsonFieldType.STRING)
                                                .description("깃허브 유저 아이디"),
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER)
                                                .description("회원 고유값"),
                                        fieldWithPath("[].description").type(JsonFieldType.STRING)
                                                .description("한줄 소개"),
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER)
                                                .description("팔로우 식별값"),
                                        fieldWithPath("[].nickName").type(JsonFieldType.STRING)
                                                .description("회원의 닉네임"),
                                        fieldWithPath("[].profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url"),
                                        fieldWithPath("[].isFollowing").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 회원이 팔로잉하고 있는 사람인지"),
                                        fieldWithPath("[].isReader").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 사람의 프로필인지")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("유저의 팔로워 리스트 조회 성공")
    void successReadMyFollowersTest() throws Exception {
        //given
        List<FollowDto> dtos = List.of(
                FollowDto.builder()
                        .id(1L)
                        .userId("haden")
                        .nickName("닉넴")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .build(),
                FollowDto.builder()
                        .id(2L)
                        .userId("thddd")
                        .nickName("사과")
                        .description("안녕하세요 ~~~ 저는")
                        .profileUrl("affd.jpg")
                        .isFollowing(true)
                        .build(),
                FollowDto.builder()
                        .id(3L)
                        .userId("agvdg")
                        .nickName("알수없음")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .build()
        );
        given(userService.readMyFollowers(any())).willReturn(dtos);
        //when
        //then
        this.mockMvc.perform(get("/users/followers")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("[].userId").type(JsonFieldType.STRING)
                                                .description("깃허브 유저 아이디"),
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER)
                                                .description("회원 고유값"),
                                        fieldWithPath("[].description").type(JsonFieldType.STRING)
                                                .description("한줄 소개"),
                                        fieldWithPath("[].nickName").type(JsonFieldType.STRING)
                                                .description("회원의 닉네임"),
                                        fieldWithPath("[].profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url"),
                                        fieldWithPath("[].isFollowing").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 회원이 팔로잉하고 있는 사람인지")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("회원의 팔로워 리스트 조회 성공")
    void successReadFollowersTest() throws Exception {
        //given
        List<FollowDto> dtos = List.of(
                FollowDto.builder()
                        .id(1L)
                        .userId("haden")
                        .nickName("닉넴")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .isReader(true)
                        .build(),
                FollowDto.builder()
                        .id(2L)
                        .userId("thddd")
                        .nickName("사과")
                        .description("안녕하세요 ~~~ 저는")
                        .profileUrl("affd.jpg")
                        .isFollowing(false)
                        .isReader(false)
                        .build(),
                FollowDto.builder()
                        .id(3L)
                        .userId("agvdg")
                        .nickName("알수없음")
                        .description("안뇽이건한줄소개")
                        .profileUrl("affd.jpg")
                        .isFollowing(true)
                        .isReader(false)
                        .build()
        );
        given(userService.readFollowers(anyLong(), any())).willReturn(dtos);
        //when
        //then
        this.mockMvc.perform(get("/users/{userId}/followers",1L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("[].userId").type(JsonFieldType.STRING)
                                                .description("깃허브 유저 아이디"),
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER)
                                                .description("회원 고유값"),
                                        fieldWithPath("[].description").type(JsonFieldType.STRING)
                                                .description("한줄 소개"),
                                        fieldWithPath("[].nickName").type(JsonFieldType.STRING)
                                                .description("회원의 닉네임"),
                                        fieldWithPath("[].profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url"),
                                        fieldWithPath("[].isFollowing").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 회원이 팔로잉하고 있는 사람인지"),
                                        fieldWithPath("[].isReader").type(JsonFieldType.BOOLEAN)
                                                .description("읽는 사람의 프로필인지")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 마이페이지 스크랩 불러오기")
    void successSearchScrap() throws Exception {
        //given
        List<SearchScrap.Response> list = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            list.add(SearchScrap.Response.builder()
                    .scrapId(1L)
                    .postId(1L)
                    .title("제목")
                    .content("내용")
                    .commentCount(i)
                    .imageUrls(getImageUrlList(3))
                    .likeCount(i)
                    .scrapCount(i)
                    .hitCount(i)
                    .postStatus(PostStatus.ACTIVE)
                    .postType(PostType.FEED)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .isLike(true)
                    .isFollow(true)
                    .scrapCreatedAt(LocalDateTime.now())
                    .build());
        }

        given(scrapService.searchScrap(any(), any()))
                .willReturn(new PageImpl<>(list));
        //when
        //then

        mockMvc.perform(get("/users/posts/scraps")
                        .param("size", "2")
                        .param("page", "0")
                        .param("sort", "POST_DATE,DESC")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].title").value("제목"))
                .andExpect(jsonPath("content[0].scrapCount").value("1"))
                .andExpect(jsonPath("content[0].content").value("내용"))
                .andExpect(jsonPath("content[0].imageUrls[0]").value("img0"))
                .andExpect(jsonPath("content[0].imageUrls[1]").value("img1"))
                .andExpect(jsonPath("content[0].postStatus").value("ACTIVE"))
                .andExpect(jsonPath("content[0].like").value(true))
                .andExpect(jsonPath("content[0].follow").value(true))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].scrapId").type(JsonFieldType.NUMBER)
                                                .description("스크랩 고유 id"),
                                        fieldWithPath("content[].postId").type(JsonFieldType.NUMBER)
                                                .description("스크랩한 게시글 고유 id"),
                                        fieldWithPath("content[].title").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 게시글 제목"),
                                        fieldWithPath("content[].imageUrls").type(
                                                        JsonFieldType.ARRAY)
                                                .description("스크랩한 게시글 이미지 링크들"),
                                        fieldWithPath("content[].content").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 게시글 본문"),
                                        fieldWithPath("content[].commentCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("스크랩한 게시글 댓글 수"),
                                        fieldWithPath("content[].likeCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("스크랩한 게시글 좋아요 수"),
                                        fieldWithPath("content[].scrapCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("스크랩한 게시글 스크랩 수"),
                                        fieldWithPath("content[].hitCount").type(
                                                        JsonFieldType.NUMBER)
                                                .description("스크랩한 게시글 죄회수"),
                                        fieldWithPath("content[].postStatus").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 게시글 상태(게시, 삭제 등)"),
                                        fieldWithPath("content[].postType").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 게시글 종류(FEED, QNA)"),
                                        fieldWithPath("content[].createdAt").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 게시글 등록일"),
                                        fieldWithPath("content[].updatedAt").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 게시글 업데이트일"),
                                        fieldWithPath("content[].like").type(JsonFieldType.BOOLEAN)
                                                .description("게시글 좋아요 클릭 여부"),
                                        fieldWithPath("content[].follow").type(JsonFieldType.BOOLEAN)
                                                .description("게시글 작성자 팔로우 여부"),
                                        fieldWithPath("content[].scrapCreatedAt").type(
                                                        JsonFieldType.STRING)
                                                .description("스크랩한 등록일"),
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
    @DisplayName("성공 - 마이페이지에서 스크랩 삭제하기")
    void successDeleteScrap() throws Exception {
        //given
        given(scrapService.removeScrap(any())).willReturn((long)4);

        //when
        //then
        mockMvc.perform(delete("/users/posts/scraps/4")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("4"))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("성공 - 마이페이지 작성한 게시글들 불러오기")
    void successSearchMyPagePosts() throws Exception {
        //given
        List<SearchMyPagePost.Response> list = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            list.add(SearchMyPagePost.Response.builder()
                    .postId(i)
                    .title("제목")
                    .postRegisterMemberId(1)
                    .postRegisterMemberId(i)
                    .imageUrls(getImageUrlArray(3))
                    .content("내용")
                    .commentCount(i)
                    .likeCount(i)
                    .scrapCount(i)
                    .hitCount(i)
                    .postStatus(PostStatus.ACTIVE)
                    .postType(PostType.FEED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isLike(true)
                    .isFollow(false)
                    .isScrap(true)
                    .build());
        }

        given(postService.searchMyPagePosts(any(), anyLong(), any(), any()))
                .willReturn(new PageImpl<>(list, PageRequest.of(0, 3), 3));

        //when
        //then
        mockMvc.perform(get("/users/2/posts")
                        .param("size", "3")
                        .param("page", "0")
                        .param("sort", "DATE,DESC")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].title").value("제목"))
                .andExpect(jsonPath("content[0].content").value("내용"))
                .andExpect(jsonPath("content[0].postRegisterMemberId").value(1))
                .andExpect(jsonPath("content[0].imageUrls[0]").value("img0"))
                .andExpect(jsonPath("content[0].imageUrls[1]").value("img1"))
                .andExpect(jsonPath("content[0].imageUrls[2]").value("img2"))
                .andExpect(jsonPath("content[0].postStatus").value("ACTIVE"))
                .andExpect(jsonPath("content[0].postType").value("FEED"))
                .andExpect(jsonPath("content[0].like").value(true))
                .andExpect(jsonPath("content[0].scrap").value(true))
                .andExpect(jsonPath("content[0].follow").value(false))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].postId").type(JsonFieldType.NUMBER)
                                                .description("게시글 고유 번호"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                                .description("게시글 제목"),
                                        fieldWithPath("content[].postRegisterMemberId").type(JsonFieldType.NUMBER)
                                                .description("게시글 작성자 고유 번호"),
                                        fieldWithPath("content[].imageUrls").type(JsonFieldType.ARRAY)
                                                .description("게시글 이미지 링크들"),
                                        fieldWithPath("content[].content").type(JsonFieldType.STRING)
                                                .description("게시글 본문"),
                                        fieldWithPath("content[].commentCount").type(JsonFieldType.NUMBER)
                                                .description("게시글 댓글 수"),
                                        fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER)
                                                .description("게시글 좋아요 수"),
                                        fieldWithPath("content[].scrapCount").type(JsonFieldType.NUMBER)
                                                .description("게시글 스크랩 수"),
                                        fieldWithPath("content[].hitCount").type(JsonFieldType.NUMBER)
                                                .description("게시글 조회수"),
                                        fieldWithPath("content[].postStatus").type(JsonFieldType.STRING)
                                                .description("게시글 상태"),
                                        fieldWithPath("content[].postType").type(JsonFieldType.STRING)
                                                .description("게시글 종류"),
                                        fieldWithPath("content[].createdAt").type(JsonFieldType.STRING)
                                                .description("게시글 등록일"),
                                        fieldWithPath("content[].updatedAt").type(JsonFieldType.STRING)
                                                .description("게시글 수정일"),
                                        fieldWithPath("content[].like").type(JsonFieldType.BOOLEAN)
                                                .description("해당 게시글 좋아요 여부"),
                                        fieldWithPath("content[].scrap").type(JsonFieldType.BOOLEAN)
                                                .description("해당 게시글 스크랩 여부"),
                                        fieldWithPath("content[].follow").type(JsonFieldType.BOOLEAN)
                                                .description("해당 게시글 작성자를 본인이 팔로우 했는지 여부"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지 여부"),
                                        fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 개수"),
                                        fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 페이지 수"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("요청 데이터 수"),
                                        fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                                                .description("현재 페이지에서 보여지는 데이터 수")
                                )
                        )
                );
    }

    private static String[] getImageUrlArray(int size) {
        String[] images = new String[size];

        for (int i = 0; i < size; i++) {
            images[i] = "img" + i;
        }

        return images;
    }

    private static List<String> getImageUrlList(int size) {
        List<String> images = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            images.add("img" + i);
        }

        return images;
    }

    @Test
    @WithMockUser
    @DisplayName("success_changeNotificationAvailable")
    void success_changeNotificationAvailable() throws Exception {
        //given
        given(notificationInfoService.changeNotificationAvailable(any(), any()))
                .willReturn("nickName");

        //when
        //then
        mockMvc.perform(put("/users/1/notification-info")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                NotificationInfoDto.builder()
                                        .isPostPushAvailable(true)
                                        .isFollowPushAvailable(false)
                                        .build()
                        ))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("nickName"))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(modifyUris().scheme(scheme).host(host).port(port), prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }
}