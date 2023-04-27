package zerobase.bud.oauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    String client_id;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    String client_secret;

    public String codeToJwt(String code) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", client_id);
        params.add("client_secret", client_secret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://github.com/login/oauth/access_token", request, String.class);

        if(ObjectUtils.isEmpty(response.getBody()) || !response.getBody().contains("access_token") || response.getBody().contains("error")) return "";
        String OAuthAccessToken = response.getBody().replace("access_token=", "").replace("&scope=&token_type=bearer", "");

        Member member = memberRepository.findByOauthToken(OAuthAccessToken).orElseThrow(() -> new BudException(ErrorCode.INVALID_TOKEN));
        return "Bearer " + tokenProvider.generateToken(member.getUserId()).getAccessToken();
    }
}
