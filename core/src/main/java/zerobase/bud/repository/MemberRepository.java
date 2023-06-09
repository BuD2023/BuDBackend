package zerobase.bud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import zerobase.bud.type.MemberStatus;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);

    Optional<Member> findByOauthToken(String oauthToken);

    List<Member> findAllByUserIdIn(Set<String> userIds);
    
    Optional<Member> findByUserCode(String userCode);

    Optional<Member> findByNickname(String nickname);

    List<Member> findAllByUserIdIn(List<String> userIds);

    Optional<Member> findByIdAndStatus(Long memberId, MemberStatus memberStatus);
}
