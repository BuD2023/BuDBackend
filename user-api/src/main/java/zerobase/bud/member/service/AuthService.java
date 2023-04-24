package zerobase.bud.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.dto.JwtDto;
import zerobase.bud.jwt.dto.RefreshDto;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.repository.LevelRepository;
import zerobase.bud.repository.MemberRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final LevelRepository levelRepository;
    private final AwsS3Api awsS3Api;

    public JwtDto login(OAuth2User oAuth2User) {
        return tokenProvider.generateToken(oAuth2User);
    }

    public JwtDto refresh(RefreshDto refreshDto) {
        boolean result = tokenProvider.validateRawToken(refreshDto.getRefreshToken());
        if (!result) {
            log.info("유효하지 않은 토큰입니다.");
            return null;
        }
        Authentication authentication = tokenProvider.getAuthentication(refreshDto.getRefreshToken());

        return tokenProvider.generateToken(authentication.getName());
    }

    public boolean addAdditionalInfo(Member member, MultipartFile file, String nickname, String job) {
        if(memberRepository.findByNickname(nickname).isPresent()) {
            throw new BudException(ErrorCode.ALREADY_USING_NICKNAME);
        }

        member.setNickname(nickname);
        member.setProfileImg(awsS3Api.getImageUrl(awsS3Api.uploadImage(file, file.getName())));
        member.setJob(job);
        member.setLevel(levelRepository.findById(1L).get());
        member.setAddInfoYn(true);
        memberRepository.save(member);
        return true;
    }
}
