package zerobase.bud.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.common.exception.MemberException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.type.MemberStatus;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.repository.FollowRepository;
import zerobase.bud.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private UserService userService;

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

    @Test
    @DisplayName("팔로우 성공")
    void successFollowTest() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .email("ddddd@gmail.com")
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .job("머머머")
                .oAuthAccessToken("tokenvalue")
                .build();


        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(targetMember));

        given(followRepository.findByTargetAndAndMember(any(), any()))
                .willReturn(Optional.empty());

        given(followRepository.save(any()))
                .willReturn(Follow.builder()
                        .member(member)
                        .target(targetMember)
                        .id(2L)
                        .build());

        //when
        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        Long targetId = userService.follow(123L, member);
        //then
        verify(followRepository, times(1)).save(captor.capture());
        assertEquals(2L, targetId);

    }

    @Test
    @DisplayName("팔로우 성공 - 이미 팔로우")
    void successFollowWhenAlreadyFollowingTest() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .email("ddddd@gmail.com")
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .job("머머머")
                .oAuthAccessToken("tokenvalue")
                .build();


        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(targetMember));

        given(followRepository.findByTargetAndAndMember(any(), any()))
                .willReturn(Optional.of(Follow.builder()
                        .member(member)
                        .target(targetMember)
                        .id(2L)
                        .build()));

        //when
        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        Long targetId = userService.follow(123L, member);
        //then
        verify(followRepository, times(1)).delete(captor.capture());
        assertEquals(2L, targetId);

    }

    @Test
    @DisplayName("팔로우 실패 - 타겟 유저 없음")
    void failFollowTest() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                ()-> userService.follow(23L, member));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

}