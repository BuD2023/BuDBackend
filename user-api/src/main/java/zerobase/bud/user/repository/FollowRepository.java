package zerobase.bud.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.bud.domain.Member;
import zerobase.bud.user.domain.Follow;

import java.util.Optional;
import java.util.stream.Stream;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByTargetAndAndMember(Member target, Member member);

    Stream<Follow> findByTarget(Member target);

    Stream<Follow> findByMember(Member member);

    Long countByTarget(Member target);

    Long countByMember(Member member);

    boolean existsByTargetAndMember(Member target, Member member);
}
