package zerobase.bud.oauth.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.domain.Level;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.jwt.dto.JwtDto;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.repository.LevelRepository;
import zerobase.bud.repository.MemberRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final static String IS_ADD_INFO = "isAddInfo";
    private final TokenProvider tokenProvider;
    private final LevelRepository levelRepository;
    private final MemberRepository memberRepository;
    private final GithubInfoRepository githubInfoRepository;
    private final AwsS3Api awsS3Api;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    String client_id;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    String client_secret;

    @Transactional
    public List<String> codeToJwt(String code) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        RestTemplate tokenTemplate = new RestTemplate();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParam = new LinkedMultiValueMap<>();
        tokenParam.add("code", code);
        tokenParam.add("client_id", client_id);
        tokenParam.add("client_secret", client_secret);
        HttpEntity<MultiValueMap<String, String>> oAuthTokenRequest = new HttpEntity<>(tokenParam, tokenHeaders);
        ResponseEntity<String> tokenResponse = tokenTemplate.postForEntity("https://github.com/login/oauth/access_token", oAuthTokenRequest, String.class);

        if(ObjectUtils.isEmpty(tokenResponse.getBody()) || !tokenResponse.getBody().contains("access_token") || tokenResponse.getBody().contains("error")) return null;
        String OAuthAccessToken = tokenResponse.getBody().replace("access_token=", "").replace("&scope=&token_type=bearer", "");

        HttpHeaders userHeaders = new HttpHeaders();
        RestTemplate userTemplate = new RestTemplate();
        MultiValueMap<String, String> userParam = new LinkedMultiValueMap<>();
        userHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer " + OAuthAccessToken);
        HttpEntity<MultiValueMap<String, String>> getUserInfoRequest = new HttpEntity<>(userParam, userHeaders);
        ResponseEntity<Map> userResponse = userTemplate.exchange("https://api.github.com/user", HttpMethod.GET, getUserInfoRequest, Map.class);

        if(ObjectUtils.isEmpty(userResponse)) return null;

        Member member = saveOrUpdate(userResponse.getBody(), OAuthAccessToken);

        return setTokenInfo(member);
    }

    private Member saveOrUpdate(Map userResponse, String token) {
        Optional<Member> optionalMember = memberRepository.findByUserId(userResponse.get("login").toString());
        Optional<GithubInfo> optionalGithubInfo = githubInfoRepository.findByUserId(userResponse.get("login").toString());
        Member member;
        GithubInfo githubInfo;

        String userId = userResponse.get("login").toString();
        String userCode = userResponse.get("id").toString();
        String nickname;

        if(ObjectUtils.isEmpty(userResponse.get("name"))) {
            nickname = userId;
        }
        else {
            nickname = userResponse.get("name").toString();
        }
        Level level =  levelRepository.findById(1L).get();

        if(optionalMember.isEmpty()) {
            Random random = new Random();
            int randNum = random.nextInt(30) + 1;
            String imageUrl = awsS3Api.getImageUrl("profiles/basic/" + randNum + ".png");

            member = Member.register(userId, userCode, token, imageUrl, level);
            githubInfo = GithubInfo.builder()
                    .userId(userId)
                    .username(nickname)
                    .accessToken(token)
                    .build();
        }
        else if(optionalGithubInfo.isEmpty()) {
            member = optionalMember.get();
            member.update(userCode, token);
            githubInfo = GithubInfo.builder()
                    .userId(userId)
                    .username(nickname)
                    .accessToken(token)
                    .build();
        }
        else {
            member = optionalMember.get();
            member.update(userCode, token);
            githubInfo = optionalGithubInfo.get();

            githubInfo.setAccessToken(token);
            githubInfo.setUsername(nickname);
        }
        memberRepository.save(member);

        githubInfo.setMember(member);
        githubInfoRepository.save(githubInfo);
        return member;
    }


    public List<String> tokenRefresh(Member member) {
        if(!tokenProvider.validateToken(member.getRefreshToken())) {
            throw new BudException(ErrorCode.INVALID_TOKEN);
        }

        return setTokenInfo(member);
    }

    private List<String> setTokenInfo(Member member) {
        List<String> tokenInfo = new ArrayList<>();
        JwtDto token = tokenProvider.generateToken(member.getUserId());
        tokenInfo.add(token.getGrantType() + token.getAccessToken());
        tokenInfo.add(member.getUserId());
        tokenInfo.add(String.valueOf(token.getAccessTokenExpiresTime()));
        member.setRefreshToken(token.getRefreshToken());
        memberRepository.save(member);

        return tokenInfo;
    }

    public Map<String, Boolean> isAddInfo(Member member) {
        return Map.of(IS_ADD_INFO, member.isAddInfoYn());

    }
}
