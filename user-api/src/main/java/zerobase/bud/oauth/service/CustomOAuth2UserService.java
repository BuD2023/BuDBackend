package zerobase.bud.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.oauth.dto.OAuthAttribute;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final GithubInfoRepository githubInfoRepository;
    private final AwsS3Api awsS3Api;
    // private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String oAuthAccessToken = userRequest.getAccessToken().getTokenValue();
        log.info("Request: OAuth2 Access Token:" + oAuthAccessToken);

        String userCode = oAuth2User.getName();
        String userNameAttributeName = userRequest
                .getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttribute attributes = OAuthAttribute.of(userNameAttributeName, oAuth2User.getAttributes(), oAuthAccessToken, userCode);
        Member member = saveOrUpdate(attributes);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(member.getStatus().getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private Member saveOrUpdate(OAuthAttribute attributes) {
        Optional<Member> optionalMember = memberRepository.findByUserId(attributes.getUserId());
        Optional<GithubInfo> optionalGithubInfo = githubInfoRepository.findByUserId(attributes.getUserId());
        Member member;
        GithubInfo githubInfo;
        if(optionalMember.isEmpty()) {
            Random random = new Random();
            int randNum = random.nextInt(30) + 1;
            String imageUrl = awsS3Api.getImageUrl("profiles/basic/" + randNum + ".png");

            member = attributes.toEntity(imageUrl);
            githubInfo = GithubInfo.builder()
                    .userId(attributes.getUserId())
                    .username(attributes.getGithubUsername())
                    .accessToken(attributes.getOAuthAccessToken())
                    .build();
        }
        else if(optionalGithubInfo.isEmpty()) {
            member = optionalMember.get();
            member.update(attributes.getUserCode(), attributes.getOAuthAccessToken());
            githubInfo = GithubInfo.builder()
                    .userId(attributes.getUserId())
                    .username(attributes.getGithubUsername())
                    .accessToken(attributes.getOAuthAccessToken())
                    .build();
        }
        else {
            member = optionalMember.get();
            member.update(attributes.getUserCode(), attributes.getOAuthAccessToken());
            githubInfo = optionalGithubInfo.get();

            githubInfo.setAccessToken(attributes.getOAuthAccessToken());
            githubInfo.setUsername(attributes.getGithubUsername());
        }
        memberRepository.save(member);

        githubInfo.setMember(member);
        githubInfoRepository.save(githubInfo);
        return member;
    }

}
