package zerobase.bud.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;
import zerobase.bud.user.domain.Follow;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByTargetAndMember(Member target, Member member);

    List<Follow> findByTarget(Member target);

    List<Follow> findByMember(Member member);

    Long countByTarget(Member target);

    Long countByMember(Member member);

    boolean existsByTargetAndMember(Member target, Member member);

    List<Follow> findAllByTargetId(Long senderId);

    void deleteAllByMember(Member member);
    void deleteAllByTarget(Member target);
}
