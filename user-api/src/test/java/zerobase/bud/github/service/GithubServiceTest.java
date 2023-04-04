package zerobase.bud.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.github.domain.CommitHistory;
import zerobase.bud.github.domain.GithubInfo;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.repository.CommitHistoryRepository;
import zerobase.bud.github.repository.GithubInfoRepository;

@ExtendWith(MockitoExtension.class)
class GithubServiceTest {

    @Mock
    private GithubInfoRepository githubInfoRepository;

    @Mock
    private CommitHistoryRepository commitHistoryRepository;

    @Mock
    private GithubApi githubApi;

    @InjectMocks
    private GithubService githubService;

    @Test
    void success_getCommitInfo() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(GithubInfo.builder()
                .id(1L)
                .accessToken("accessToken")
                .email("abcd@naver.com")
                .userName("userName")
                .build()));

        given(commitHistoryRepository
            .findAllByGithubInfoIdOrderByCommitDateDesc(anyLong()))
            .willReturn(List.of(CommitHistory.builder()
                .commitDate(LocalDate.now())
                .consecutiveCommitDays(1L)
                .commitCount(3L)
                .build()));
        //when
        CommitHistoryInfo info = githubService.getCommitInfo(
            "abcd@naver.com");
        //then
        assertEquals(3, info.getTotalCommitCount());
        assertEquals(3, info.getTodayCommitCount());
        assertEquals(3, info.getThisWeekCommitCount());
        assertEquals(1, info.getConsecutiveCommitDays());
        assertEquals(LocalDate.now(), info.getCommits().get(0).getCommitDate());
        assertEquals(3, info.getCommits().get(0).getCommitCount());
    }

    @Test
    void success_getCommitInfo_empty() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(GithubInfo.builder()
                .id(1L)
                .accessToken("accessToken")
                .email("abcd@naver.com")
                .userName("userName")
                .build()));

        given(commitHistoryRepository
            .findAllByGithubInfoIdOrderByCommitDateDesc(anyLong()))
            .willReturn(List.of());
        //when
        CommitHistoryInfo info = githubService.getCommitInfo(
            "abcd@naver.com");
        //then
        assertEquals(0, info.getTotalCommitCount());
        assertEquals(0, info.getTodayCommitCount());
        assertEquals(0, info.getThisWeekCommitCount());
        assertEquals(0, info.getConsecutiveCommitDays());
    }

    @Test
    @DisplayName("NOT_REGISTERED_MEMBER_getCommitInfo_empty")
    void NOT_REGISTERED_MEMBER_getCommitInfo_empty() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubService.getCommitInfo(
                "abcd@naver.com"));
        //then
        assertEquals(NOT_REGISTERED_MEMBER, budException.getErrorCode());
    }

    @Test
    void saveCommitInfoFromLastCommitDate() {
        //given
        given(githubApi.saveCommitInfoFromLastCommitDate(anyString(),
            anyString()))
            .willReturn("success");
        //when
        String user = githubService.saveCommitInfoFromLastCommitDate(
            "email@naver.com", "user");
        //then
        assertEquals("success", user);
    }
}