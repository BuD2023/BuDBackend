package zerobase.bud.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.FAILED_CONNECT_GITHUB;
import static zerobase.bud.common.type.ErrorCode.FAILED_GET_COMMIT_INFO;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.github.domain.CommitHistory;
import zerobase.bud.github.domain.GithubInfo;
import zerobase.bud.github.repository.CommitHistoryRepository;
import zerobase.bud.github.repository.GithubInfoRepository;

@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
class GithubApiTest {

    @Mock
    private GithubInfoRepository githubInfoRepository;

    @Mock
    private CommitHistoryRepository commitHistoryRepository;

    @InjectMocks
    private GithubApi githubApi;

//    @Test
//    void success_saveCommitInfoFromLastCommitDate() {
//        //given
//        given(githubInfoRepository.findByEmail(anyString()))
//            .willReturn(Optional.ofNullable(getGithubInfo()));
//
//        given(commitHistoryRepository
//            .findFirstByGithubInfoIdOrderByCommitDateDesc(anyLong()))
//            .willReturn(Optional.ofNullable(
//                getCommitHistory()));
//
//        given(commitHistoryRepository
//            .findByGithubInfoIdAndCommitDate(anyLong(), any()))
//            .willReturn(Optional.of(getCommitHistory()));
//
//        //when
//        String email = githubApi.saveCommitInfoFromLastCommitDate(
//            "<fakeEmail>", "<RealUsername>", getGithubInfo(), LocalDate.now());
//        //then
//        assertEquals("abcd@naver.com", email);
//    }

    @Test
    @DisplayName("NOT_REGISTERED_MEMBER_saveCommitInfoFromLastCommitDate")
    void NOT_REGISTERED_MEMBER_saveCommitInfoFromLastCommitDate() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubApi.saveCommitInfoFromLastCommitDate(
                "<fakeEmail>", "<fakeUsername>", getGithubInfo(),
                LocalDate.now()));
        //then
        assertEquals(NOT_REGISTERED_MEMBER, budException.getErrorCode());
    }

    @Test
    @DisplayName("FAILED_CONNECT_GITHUB_saveCommitInfoFromLastCommitDate")
    void FAILED_CONNECT_GITHUB_saveCommitInfoFromLastCommitDate() {
        //given
        given(githubInfoRepository.findByEmail(anyString()))
            .willReturn(Optional.ofNullable(GithubInfo.builder()
                .id(1L)
                .accessToken("faultAccessToken")
                .createdAt(LocalDateTime.now())
                .build()));

        //when
        BudException budException = assertThrows(BudException.class,
            () -> githubApi.saveCommitInfoFromLastCommitDate(
                "<fakeEmail>", "<fakeUsername>", getGithubInfo(),
                LocalDate.now()));
        //then
        assertEquals(FAILED_CONNECT_GITHUB, budException.getErrorCode());
    }

//    @Test
//    @DisplayName("FAILED_GET_COMMIT_INFO_saveCommitInfoFromLastCommitDate")
//    void FAILED_GET_COMMIT_INFO_saveCommitInfoFromLastCommitDate() {
//        //given
//        given(githubInfoRepository.findByEmail(anyString()))
//            .willReturn(Optional.ofNullable(getGithubInfo()));
//
//        //when
//        BudException budException = assertThrows(BudException.class,
//            () -> githubApi.saveCommitInfoFromLastCommitDate(
//                "<fakeEmail>", "<fakeUsername>", getGithubInfo(),
//                LocalDate.now()));
//        //then
//        assertEquals(FAILED_GET_COMMIT_INFO, budException.getErrorCode());
//    }

    private static GithubInfo getGithubInfo() {
        return GithubInfo.builder()
            .id(1L)
            .accessToken("<RealAccessToken>")
            .userName("<RealName>")
            .email("<RealEmail>")
            .createdAt(LocalDateTime.now())
            .build();
    }

    private static CommitHistory getCommitHistory() {
        return CommitHistory.builder()
            .id(1L)
            .githubInfo(getGithubInfo())
            .commitDate(LocalDate.now())
            .commitCount(3L)
            .consecutiveCommitDays(2L)
            .build();
    }
}