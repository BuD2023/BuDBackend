package zerobase.bud.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.dto.JwtDto;
import zerobase.bud.jwt.dto.RefreshDto;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.member.dto.MemberDto;
import zerobase.bud.repository.MemberRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

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

    public boolean addAdditionalInfo(String token, MemberDto.Info parameter) {
        String userId = tokenProvider.getUserIdInRawToken(token);
        if(ObjectUtils.isEmpty(userId)) {
            return false;
        }
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);

        if(!optionalMember.isPresent()) {
            return false;
        }

        Member member = optionalMember.get();
        member.setNickname(parameter.getNickname());
        member.setProfileImg(parameter.getProfileImg());
        member.setJob(parameter.getJob());
        member.setAddInfoYn(true);
        memberRepository.save(member);
        return true;
    }
}
