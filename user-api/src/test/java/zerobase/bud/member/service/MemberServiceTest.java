package zerobase.bud.member.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.user.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private AwsS3Api awsS3Api;

    @InjectMocks
    private MemberService memberService;

    @Test
    void getProfileRandomImage() {
        String profileRandomImage = memberService.getProfileRandomImage();
        assertNotNull(profileRandomImage);
    }
}