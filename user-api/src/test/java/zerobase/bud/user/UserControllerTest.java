package zerobase.bud.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import zerobase.bud.domain.Member;
import zerobase.bud.security.TokenProvider;
import zerobase.bud.type.MemberStatus;
import zerobase.bud.user.controller.UserController;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
class UserControllerTest {

    @MockBean
    private UserService userService;

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

        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .email("abcde@gmail.com")
                .profileImg("abcde.jpg")
                .nickname("안뇽")
                .job("시스템프로그래머")
                .oAuthAccessToken("tokenvalue")
                .build();

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Authentication authentication = new UsernamePasswordAuthenticationToken(member, "",
                List.of(MemberStatus.VERIFIED.getKey()).stream().map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));

        given(tokenProvider.getAuthentication("임의의토큰")).willReturn(authentication);
    }

    @Test
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
                        .level("씩씩한사람")
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
                                        fieldWithPath("level").type(JsonFieldType.STRING)
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
    @DisplayName("나의 프로필 조회 성공")
    void successReadMyProfileTest() throws Exception {
        //given
        given(userService.readMyProfile(any())).willReturn(
                UserDto.builder().userId("thdefn")
                        .description("암생각없다")
                        .id(1L)
                        .nickName("닉넴")
                        .level("씩씩한사람")
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
                                        fieldWithPath("level").type(JsonFieldType.STRING)
                                                .description("회원의 레벨"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING)
                                                .description("회원의 프로필 url")
                                )
                        )
                );
    }

}