package zerobase.bud.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.bud.domain.Member;
import zerobase.bud.user.domain.Follow;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByTargetAndAndMember(Member target, Member member);
}
