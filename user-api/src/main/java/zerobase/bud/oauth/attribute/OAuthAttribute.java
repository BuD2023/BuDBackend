package zerobase.bud.oauth.attribute;

import lombok.Builder;
import lombok.Getter;
import zerobase.bud.domain.Member;
import zerobase.bud.type.MemberStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class OAuthAttribute {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String userId;
    private final String nickname;
    private final String email;
    private final String oAuthAccessToken;

    @Builder
    public OAuthAttribute(Map<String, Object> attributes, String nameAttributeKey, String userId, String nickname, String email, String oAuthAccessToken) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.oAuthAccessToken = oAuthAccessToken;
    }

    public static OAuthAttribute of(String userNameAttributeName, Map<String, Object> attributes, String oAuthAccessToken) {
        return OAuthAttribute.builder()
                .userId((String) attributes.get("login"))
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name"))
                .oAuthAccessToken(oAuthAccessToken)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .userId(userId)
                .nickname(nickname)
                .email(email)
                .oAuthAccessToken(oAuthAccessToken)
                .status(MemberStatus.VERIFIED)
                .createdAt(LocalDateTime.now())
                .build();
    }


}
