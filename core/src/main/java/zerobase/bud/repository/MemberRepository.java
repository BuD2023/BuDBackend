package zerobase.bud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);

    Optional<Member> findByUserCode(String userCode);

    Optional<Member> findByNickname(String nickname);

    List<Member> findAllByUserIdIn(List<String> userIds);
}
