package zerobase.bud.github.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.github.dto.CommitCountByDate;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubService;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.service.GithubApi;

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

    private final static String TOKEN = "BEARER TOKEN";

    @Test
    @WithMockUser
    void success_saveCommitInfoFromLastCommitDate() throws Exception {
        //given 어떤 데이터가 주어졌을 때
        given(githubService.saveCommitInfoFromLastCommitDate(any()))
            .willReturn("success");

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(post("/github")
                .header(HttpHeaders.AUTHORIZATION, TOKEN)
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
    void success_getCommitInfo() throws Exception {
        //given 어떤 데이터가 주어졌을 때

        given(githubService.getCommitInfo(any()))
            .willReturn(CommitHistoryInfo.builder()
                .nickName("nick")
                .levelCode("씩씩한_새싹")
                .imagePath("images/level1.png")
                .remainCommitCountNextLevel(1L)
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
                get("/github")
                    .header(HttpHeaders.AUTHORIZATION, TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickName").value("nick"))
            .andExpect(jsonPath("$.levelCode").value("씩씩한_새싹"))
            .andExpect(jsonPath("$.imagePath").value("images/level1.png"))
            .andExpect(jsonPath("$.remainCommitCountNextLevel").value(1))
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