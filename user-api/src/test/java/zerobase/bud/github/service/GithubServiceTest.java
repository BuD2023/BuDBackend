package zerobase.bud.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.CommitHistory;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.repository.CommitHistoryRepository;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.service.GithubApi;

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
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(commitHistoryRepository
            .findAllByGithubInfoIdOrderByCommitDateDesc(anyLong()))
            .willReturn(List.of(getCommitHistory()));
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

    private static CommitHistory getCommitHistory() {
        return CommitHistory.builder()
            .commitDate(LocalDate.now())
            .consecutiveCommitDays(1L)
            .commitCount(3L)
            .build();
    }

    private static GithubInfo getGithubInfo() {
        return GithubInfo.builder()
            .id(1L)
            .accessToken("accessToken")
            .email("abcd@naver.com")
            .username("userName")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void success_getCommitInfo_empty() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

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
    void success_saveCommitInfoFromLastCommitDate() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(githubApi.saveCommitInfoFromLastCommitDate(any(), any()))
            .willReturn("success");

        given(
            commitHistoryRepository.findFirstByGithubInfoIdOrderByCommitDateDesc(
                anyLong()))
            .willReturn(Optional.ofNullable(getCommitHistory()));
        //when
        String user = githubService.saveCommitInfoFromLastCommitDate(
            "email@naver.com");
        //then
        assertEquals("success", user);
    }

    @Test
    @DisplayName("NOT_REGISTERED_MEMBER_saveCommitInfoFromLastCommitDate")
    void NOT_REGISTERED_MEMBER_saveCommitInfoFromLastCommitDate() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubService.saveCommitInfoFromLastCommitDate(
                "email@naver.com"));
        //then
        assertEquals(NOT_REGISTERED_MEMBER, budException.getErrorCode());
    }
}