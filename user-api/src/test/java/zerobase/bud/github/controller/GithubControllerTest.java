package zerobase.bud.github.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import zerobase.bud.domain.Member;
import zerobase.bud.github.dto.CommitCountByDate;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubService;
import zerobase.bud.security.TokenProvider;
import zerobase.bud.service.GithubApi;
import zerobase.bud.type.MemberStatus;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(GithubController.class)
@AutoConfigureRestDocs
class GithubControllerTest {

    @MockBean
    private GithubApi githubApi;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private GithubService githubService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String token = "token";

    @BeforeEach
    void init(
        WebApplicationContext context,
        RestDocumentationContextProvider contextProvider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(documentationConfiguration(contextProvider))
            .addFilters(new CharacterEncodingFilter("UTF-8", true))
            .alwaysDo(print())
            .build();

        Member member = Member.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .status(MemberStatus.VERIFIED)
            .email("xxxx@naver.com")
            .profileImg("abcde.jpg")
            .nickname("nickname")
            .job("Job")
            .oAuthAccessToken("token")
            .build();

        objectMapper.setVisibility(PropertyAccessor.FIELD,
            JsonAutoDetect.Visibility.ANY);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            member, "",
            Stream.of(MemberStatus.VERIFIED.getKey()).map(
                    SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));

        given(tokenProvider.getAuthentication("token")).willReturn(
            authentication);
    }


    @Test
    void success_saveCommitInfoFromLastCommitDate() throws Exception {
        //given 어떤 데이터가 주어졌을 때
        given(githubService.saveCommitInfoFromLastCommitDate(
            anyString()))
            .willReturn("success");

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(post("/home/github")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );
    }

    @Test
    void success_getCommitInfo() throws Exception {
        //given 어떤 데이터가 주어졌을 때
        given(githubService.getCommitInfo(anyString()))
            .willReturn(CommitHistoryInfo.builder()
                .totalCommitCount(1L)
                .thisWeekCommitCount(3L)
                .consecutiveCommitDays(2L)
                .todayCommitCount(1L)
                .commits(List.of(
                    CommitCountByDate.builder()
                        .commitCount(1L)
                        .commitDate(LocalDate.now())
                        .build()
                ))
                .build());

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(
                get("/home/github/info").header(HttpHeaders.AUTHORIZATION, token))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCommitCount").value(1))
            .andExpect(jsonPath("$.todayCommitCount").value(1))
            .andExpect(jsonPath("$.thisWeekCommitCount").value(3))
            .andExpect(jsonPath("$.consecutiveCommitDays").value(2))
            .andExpect(jsonPath("$.commits[0].commitCount").value(1))
            .andDo(
                document("{class-name}/{method-name}",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()))
            );
    }


}