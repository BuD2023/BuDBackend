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
import zerobase.bud.domain.Level;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.service.SendNotificationService;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.type.MemberStatus;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.dto.FollowDto;
import zerobase.bud.user.dto.UserDto;
import zerobase.bud.user.repository.FollowRepository;
import zerobase.bud.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private PostRepository postRepository;

    @Mock
    private SendNotificationService sendNotificationService;

    @InjectMocks
    private UserService userService;

    Level level = Level.builder()
            .id(1L)
            .levelCode("씩씩한사람")
            .levelStartCommitCount(0)
            .nextLevelStartCommitCount(17)
            .build();

    Level level2 = Level.builder()
            .id(1L)
            .levelCode("씩씩하지않은새싹")
            .levelStartCommitCount(0)
            .nextLevelStartCommitCount(17)
            .build();


    Member member = Member.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .status(MemberStatus.VERIFIED)
            .introduceMessage("안녕나는나는")
            .level(level)
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
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .job("머머머")
                .oAuthAccessToken("tokenvalue")
                .build();


        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(targetMember));

        given(followRepository.findByTargetAndMember(any(), any()))
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
    void successFollowTest_AlreadyFollowing() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .job("머머머")
                .oAuthAccessToken("tokenvalue")
                .build();


        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(targetMember));

        given(followRepository.findByTargetAndMember(any(), any()))
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
    @DisplayName("팔로우 실패 - 자기 자신을 팔로우")
    void failFollowTest_FollowSelf() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .job("머머머")
                .oAuthAccessToken("tokenvalue")
                .build();


        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(targetMember));

        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> userService.follow(23L, targetMember));
        //then
        assertEquals(ErrorCode.CANNOT_FOLLOW_YOURSELF, exception.getErrorCode());

    }

    @Test
    @DisplayName("팔로우 실패 - 타겟 유저 없음")
    void failFollowTest() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> userService.follow(23L, member));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 프로필 조회 성공")
    void successReadProfileTest() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세요 저는 어쩌구저쩌구")
                .level(level)
                .job("시스템프로그래머")
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .build();


        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(targetMember));

        given(followRepository.countByMember(any())).willReturn(3L);
        given(followRepository.countByTarget(any())).willReturn(0L);
        given(postRepository.countByMember(any())).willReturn(23L);
        given(followRepository.existsByTargetAndMember(any(), any())).willReturn(true);
        //when
        UserDto dto = userService.readProfile(12L, member);
        //then
        assertEquals(true, dto.getIsFollowing());
        assertEquals(false, dto.getIsReader());
        assertEquals(23L, dto.getNumberOfPosts());
        assertEquals(0L, dto.getNumberOfFollowers());
        assertEquals(3L, dto.getNumberOfFollows());
        assertEquals("하이", dto.getNickName());
        assertEquals("안녕하세요 저는 어쩌구저쩌구", dto.getDescription());
        assertEquals("ddddd.jpg", dto.getProfileUrl());
    }

    @Test
    @DisplayName("회원의 프로필 조회 실패 - 타겟 유저 없음")
    void failReadProfileTest() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> userService.readProfile(23L, member));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }

    @Test
    @DisplayName("나의 프로필 조회 성공")
    void successReadMyProfileTest() {
        //given
        given(followRepository.countByMember(any())).willReturn(3L);
        given(followRepository.countByTarget(any())).willReturn(0L);
        given(postRepository.countByMember(any())).willReturn(23L);
        //when
        UserDto dto = userService.readMyProfile(member);
        //then
        assertEquals(23L, dto.getNumberOfPosts());
        assertEquals(0L, dto.getNumberOfFollowers());
        assertEquals(3L, dto.getNumberOfFollows());
        assertEquals("안뇽", dto.getNickName());
        assertEquals("안녕나는나는", dto.getDescription());
        assertEquals("abcde.jpg", dto.getProfileUrl());
    }

    @Test
    @DisplayName("나의 팔로우 리스트 조회 성공")
    void successReadMyFollowingsTest() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세요 저는 어쩌구저쩌구")
                .level(level)
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .build();

        Member targetMember2 = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세용")
                .level(level2)
                .profileImg("ddddd.jpg")
                .nickname("닉넴고갈")
                .build();

        List<Follow> follows = List.of(
                Follow.builder()
                        .id(1L)
                        .target(targetMember)
                        .member(member)
                        .build(),

                Follow.builder()
                        .id(1L)
                        .target(targetMember2)
                        .member(member)
                        .build()
        );
        given(followRepository.findByMember(any())).willReturn(follows);
        //when
        List<FollowDto> followDtos = userService.readMyFollowings(member);
        //then
        assertEquals(true, followDtos.get(0).getIsFollowing());
        assertEquals("안녕하세요 저는 어쩌구저쩌구", followDtos.get(0).getDescription());
        assertEquals("ddddd.jpg", followDtos.get(0).getProfileUrl());
        assertEquals(2L, followDtos.get(0).getId());
        assertEquals("하이", followDtos.get(0).getNickName());

    }

    @Test
    @DisplayName("나의 팔로워 리스트 조회 성공")
    void successReadMyFollowersTest() {
        //given
        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세요 저는 어쩌구저쩌구")
                .level(level)
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .build();

        Member targetMember2 = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세용")
                .level(level2)
                .profileImg("ddddd.jpg")
                .nickname("닉넴고갈")
                .build();

        List<Follow> follows = List.of(
                Follow.builder()
                        .id(1L)
                        .target(targetMember)
                        .member(member)
                        .build(),

                Follow.builder()
                        .id(1L)
                        .target(targetMember2)
                        .member(member)
                        .build()
        );
        given(followRepository.findByTarget(any())).willReturn(follows);
        //when
        List<FollowDto> followDtos = userService.readMyFollowers(member);
        //then
        assertEquals(true, followDtos.get(0).getIsFollowing());
        assertEquals("안녕나는나는", followDtos.get(0).getDescription());
        assertEquals("abcde.jpg", followDtos.get(0).getProfileUrl());
        assertEquals(1L, followDtos.get(0).getId());
        assertEquals("안뇽", followDtos.get(0).getNickName());

    }

    @Test
    @DisplayName("회원의 팔로우 리스트 조회 성공")
    void successReadFollowingsTest() {
        //given
        Member profileMember = Member.builder()
                .id(4L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("암튼없음")
                .level(level2)
                .profileImg("ddddd.jpg")
                .nickname("닉넴없음")
                .build();

        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세요 저는 어쩌구저쩌구")
                .level(level)
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .build();

        List<Follow> follows = List.of(
                Follow.builder()
                        .id(1L)
                        .target(member)
                        .member(profileMember)
                        .build(),

                Follow.builder()
                        .id(1L)
                        .target(targetMember)
                        .member(profileMember)
                        .build()
        );

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(profileMember));
        given(followRepository.findByMember(any())).willReturn(follows);
        given(followRepository.existsByTargetAndMember(member, member))
                .willReturn(false);
        given(followRepository.existsByTargetAndMember(targetMember, member))
                .willReturn(true);
        //when
        List<FollowDto> followDtos = userService.readFollowings(5L, member);
        //then
        assertEquals(false, followDtos.get(0).getIsFollowing());
        assertEquals(true, followDtos.get(0).getIsReader());
        assertEquals("안녕나는나는", followDtos.get(0).getDescription());
        assertEquals("abcde.jpg", followDtos.get(0).getProfileUrl());
        assertEquals(1L, followDtos.get(0).getId());
        assertEquals("안뇽", followDtos.get(0).getNickName());

    }

    @Test
    @DisplayName("회원의 팔로워 리스트 조회 성공")
    void successReadFollowersTest() {
        //given
        Member profileMember = Member.builder()
                .id(4L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("암튼없음")
                .level(level2)
                .profileImg("ddddd.jpg")
                .nickname("닉넴없음")
                .build();

        Member targetMember = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .introduceMessage("안녕하세요 저는 어쩌구저쩌구")
                .level(level)
                .profileImg("ddddd.jpg")
                .nickname("하이")
                .build();

        List<Follow> follows = List.of(
                Follow.builder()
                        .id(1L)
                        .target(profileMember)
                        .member(member)
                        .build(),

                Follow.builder()
                        .id(1L)
                        .target(profileMember)
                        .member(targetMember)
                        .build()
        );

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(profileMember));
        given(followRepository.findByTarget(any())).willReturn(follows);
        given(followRepository.existsByTargetAndMember(member, member))
                .willReturn(false);
        given(followRepository.existsByTargetAndMember(targetMember, member))
                .willReturn(true);
        //when
        List<FollowDto> followDtos = userService.readFollowers(5L, member);
        //then
        assertEquals(false, followDtos.get(0).getIsFollowing());
        assertEquals(true, followDtos.get(0).getIsReader());
        assertEquals("안녕나는나는", followDtos.get(0).getDescription());
        assertEquals("abcde.jpg", followDtos.get(0).getProfileUrl());
        assertEquals(1L, followDtos.get(0).getId());
        assertEquals("안뇽", followDtos.get(0).getNickName());

    }

    @Test
    @DisplayName("회원의 팔로우 리스트 실패 - 타겟 유저 없음")
    void failReadFollowingsTest() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> userService.readFollowings(1L, member));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원의 팔로워 리스트 실패 - 타겟 유저 없음")
    void failReadFollowersTest() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> userService.readFollowers(1L, member));
        //then
        assertEquals(ErrorCode.NOT_REGISTERED_MEMBER, exception.getErrorCode());
    }


}