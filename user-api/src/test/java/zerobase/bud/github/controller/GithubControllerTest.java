package zerobase.bud.github.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.github.dto.CommitCountByDate;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.service.GithubApi;
import zerobase.bud.github.service.GithubService;

@WebMvcTest(GithubController.class)
class GithubControllerTest {

    @MockBean
    private GithubApi githubApi;

    @MockBean
    private GithubService githubService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void success_saveCommitInfoFromLastCommitDate() throws Exception {
        //given 어떤 데이터가 주어졌을 때
        given(githubService.saveCommitInfoFromLastCommitDate(
            anyString(),
            anyString()))
            .willReturn("success");

        //when 어떤 경우에
        //then 이런 결과가 나온다.
        mockMvc.perform(post("/home/github")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
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
        mockMvc.perform(get("/home/github/info"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCommitCount").value(1))
            .andExpect(jsonPath("$.todayCommitCount").value(1))
            .andExpect(jsonPath("$.thisWeekCommitCount").value(3))
            .andExpect(jsonPath("$.consecutiveCommitDays").value(2))
            .andExpect(jsonPath("$.commits[0].commitCount").value(1));
    }


}