package zerobase.bud.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;

import static zerobase.bud.util.Constants.TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String getUserId(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

        Claims claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());
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
