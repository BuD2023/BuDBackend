package zerobase.bud.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    VERIFIED("ROLE_VERIFIED", "인증된 회원"),
    BLOCKED("ROLE_BLOCKED", "차단된 회원");

    private final String key;
    private final String status;
}
