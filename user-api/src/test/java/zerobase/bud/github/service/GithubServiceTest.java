package zerobase.bud.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.INVALID_INITIAL_VALUE;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_GITHUB_USER_ID;

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
import zerobase.bud.domain.Level;
import zerobase.bud.domain.Member;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.repository.CommitHistoryRepository;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.repository.LevelRepository;
import zerobase.bud.service.GithubApi;

@ExtendWith(MockitoExtension.class)
class GithubServiceTest {

    @Mock
    private GithubInfoRepository githubInfoRepository;

    @Mock
    private CommitHistoryRepository commitHistoryRepository;

    @Mock
    private LevelRepository levelRepository;


    @Mock
    private GithubApi githubApi;

    @InjectMocks
    private GithubService githubService;

    @Test
    void success_getCommitInfo() {
        //given
        given(githubInfoRepository.findByUserId(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(commitHistoryRepository
            .findAllByGithubInfoIdAndCommitDateBetweenOrderByCommitDateDesc(
                anyLong(), any(), any()))
            .willReturn(List.of(getCommitHistory()));

        given(
            levelRepository.findByLevelStartCommitCountLessThanEqualAndNextLevelStartCommitCountGreaterThan(
                anyLong(), anyLong()))
            .willReturn(Optional.ofNullable(getLevel()));
        //when
        CommitHistoryInfo info = githubService.getCommitInfo(
            getMember());
        //then
        assertEquals("nick", info.getNickName());
        assertEquals("씩씩한_새싹", info.getLevelCode());
        assertEquals(14, info.getRemainCommitCountNextLevel());
        assertEquals(3, info.getTodayCommitCount());
        assertEquals(3, info.getThisWeekCommitCount());
        assertEquals(1, info.getConsecutiveCommitDays());
        assertEquals(LocalDate.now(), info.getCommits().get(0).getCommitDate());
        assertEquals(3, info.getCommits().get(0).getCommitCount());
    }

    @Test
    void success_getCommitInfo_empty() {
        //given
        given(githubInfoRepository.findByUserId(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(commitHistoryRepository
            .findAllByGithubInfoIdAndCommitDateBetweenOrderByCommitDateDesc(
                anyLong(), any(), any()))
            .willReturn(List.of());

        given(levelRepository.findByLevelStartCommitCount(anyLong()))
            .willReturn(Optional.ofNullable(getLevel()));
        //when
        CommitHistoryInfo info = githubService.getCommitInfo(
            getMember());
        //then
        assertEquals("nick", info.getNickName());
        assertEquals("씩씩한_새싹", info.getLevelCode());
        assertEquals(17, info.getRemainCommitCountNextLevel());
        assertEquals(0, info.getTodayCommitCount());
        assertEquals(0, info.getThisWeekCommitCount());
        assertEquals(0, info.getConsecutiveCommitDays());
    }

    @Test
    @DisplayName("NOT_REGISTERED_GITHUB_USER_ID_getCommitInfo")
    void NOT_REGISTERED_GITHUB_USER_ID_getCommitInfo() {
        //given
        given(githubInfoRepository.findByUserId(anyString()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubService.getCommitInfo(
                getMember()));
        //then
        assertEquals(NOT_REGISTERED_GITHUB_USER_ID,
            budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_INITIAL_VALUE_getCommitInfo_empty")
    void INVALID_INITIAL_VALUE_getCommitInfo_empty() {
        //given
        given(githubInfoRepository.findByUserId(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(commitHistoryRepository
            .findAllByGithubInfoIdAndCommitDateBetweenOrderByCommitDateDesc(
                anyLong(), any(), any()))
            .willReturn(List.of());

        given(levelRepository.findByLevelStartCommitCount(anyLong()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubService.getCommitInfo(
                getMember()));
        //then
        assertEquals(INVALID_INITIAL_VALUE, budException.getErrorCode());
    }

    @Test
    void success_saveCommitInfoFromLastCommitDate() {
        //given
        given(githubInfoRepository.findByUserId(anyString()))
            .willReturn(Optional.ofNullable(getGithubInfo()));

        given(githubApi.saveCommitInfoFromLastCommitDate(any(), any()))
            .willReturn("success");

        given(
            commitHistoryRepository.findFirstByGithubInfoIdOrderByCommitDateDesc(
                anyLong()))
            .willReturn(Optional.ofNullable(getCommitHistory()));
        //when
        String user = githubService.saveCommitInfoFromLastCommitDate(
            getMember());
        //then
        assertEquals("success", user);
    }

    @Test
    @DisplayName("NOT_REGISTERED_MEMBER_saveCommitInfoFromLastCommitDate")
    void NOT_REGISTERED_MEMBER_saveCommitInfoFromLastCommitDate() {
        //given
        given(githubInfoRepository.findByUserId(anyString()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubService.saveCommitInfoFromLastCommitDate(
                getMember()));
        //then
        assertEquals(NOT_REGISTERED_GITHUB_USER_ID,
            budException.getErrorCode());
    }

    private Member getMember() {
        return Member.builder()
            .nickname("nick")
            .level(getLevel())
            .userId("")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static Level getLevel() {
        return Level.builder()
            .levelCode("씩씩한_새싹")
            .levelStartCommitCount(0)
            .nextLevelStartCommitCount(17)
            .levelNumber(1)
            .build();
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
            .username("userName")
            .createdAt(LocalDateTime.now())
            .build();
    }
}