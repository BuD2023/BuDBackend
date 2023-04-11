package zerobase.bud.oauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import zerobase.bud.domain.Member;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.oauth.dto.OAuthAttribute;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    // private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String oAuthAccessToken = userRequest.getAccessToken().getTokenValue();
        String userNameAttributeName = userRequest
                .getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttribute attributes = OAuthAttribute.of(userNameAttributeName, oAuth2User.getAttributes(), oAuthAccessToken);
        Member member = saveOrUpdate(attributes);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(member.getStatus().getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private Member saveOrUpdate(OAuthAttribute attributes) {
        Optional<Member> optionalMember = memberRepository.findByUserId(attributes.getUserId());
        Member member;
        if(!optionalMember.isPresent()) {
            member = attributes.toEntity();
        }
        else {
            member = optionalMember.get();
            member.update(attributes.getEmail(), attributes.getNickname(), attributes.getOAuthAccessToken());
        }
        memberRepository.save(member);
        return member;
    }
}
