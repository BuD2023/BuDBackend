package zerobase.bud.jwt;

import static zerobase.bud.jwt.util.JwtConstants.TOKEN_PREFIX;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import zerobase.bud.domain.Member;
import zerobase.bud.jwt.dto.JwtDto;
import zerobase.bud.member.service.MemberService;
import zerobase.bud.repository.MemberRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${spring.jwt.secret}")
    private String secretKey;

    private static final String KEY_ROLE = "role";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    public JwtDto generateToken(String userId) {
        Optional<Member> optionalReader = memberRepository.findByUserId(userId);
        if (!optionalReader.isPresent()) {
            log.info("존재하지 않는 사용자입니다.");
            return null;
        }
        Member member = optionalReader.get();

        return setJwtDto(userId, member.getStatus().getKey());
    }

    public JwtDto generateToken(OAuth2User oAuth2User) {
        String userId = oAuth2User.getAttribute("login");
        String role = oAuth2User.getAuthorities().toString();
        return setJwtDto(userId, role);
    }

    private JwtDto setJwtDto(String userId, String role) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put(KEY_ROLE, role);

        Date now = new Date();
        Date accessTokenExpiredTime = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(accessTokenExpiredTime)
                .signWith(SignatureAlgorithm.HS512, this.secretKey)
                .compact();
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, this.secretKey)
                .compact();
        return JwtDto.builder()
                .grantType("Bearer ")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresTime(accessTokenExpiredTime)
                .build();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = memberService.loadUserByUsername(this.getUserId(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserId(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

        Claims claims = this.parseClaims(token);
//        return !claims.getExpiration().before(new Date());
        return true;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String getUserIdInRawToken(String rawToken) {
        if (!ObjectUtils.isEmpty(rawToken) && rawToken.startsWith(TOKEN_PREFIX)) {
            String token = rawToken.substring(TOKEN_PREFIX.length());
            return getUserId(token);
        }
        return null;
    }

    public boolean validateRawToken(String rawToken) {
        if (!ObjectUtils.isEmpty(rawToken) && rawToken.startsWith(TOKEN_PREFIX)) {
            String token = rawToken.substring(TOKEN_PREFIX.length());
            return validateToken(token);
        }
        return false;
    }
}
