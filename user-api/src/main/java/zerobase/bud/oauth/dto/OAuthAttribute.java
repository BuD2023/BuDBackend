package zerobase.bud.oauth.dto;

import lombok.Builder;
import lombok.Getter;
import zerobase.bud.domain.Member;
import zerobase.bud.type.MemberStatus;

import java.util.Map;

@Getter
public class OAuthAttribute {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String userId;
    private final String userCode;
    private final String email;
    private final String oAuthAccessToken;

    @Builder
    public OAuthAttribute(Map<String, Object> attributes, String nameAttributeKey, String userId, String email, String oAuthAccessToken, String userCode) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.userId = userId;
        this.userCode = userCode;
        this.email = email;
        this.oAuthAccessToken = oAuthAccessToken;
    }

    public static OAuthAttribute of(String userNameAttributeName, Map<String, Object> attributes, String oAuthAccessToken, String userCode) {
        return OAuthAttribute.builder()
                .userId((String) attributes.get("login"))
                .email((String) attributes.get("email"))
                .userCode(userCode)
                .oAuthAccessToken(oAuthAccessToken)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public Member toEntity(String imageUrl) {
        return Member.builder()
                .userId(userId)
                .userCode(userCode)
                .status(MemberStatus.VERIFIED)
                .profileImg(imageUrl)
                .addInfoYn(false)
                .build();
    }


}
