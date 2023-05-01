package zerobase.bud.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;
import zerobase.bud.type.MemberStatus;
import zerobase.bud.user.domain.Follow;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByTargetAndMember(Member target, Member member);

    List<Follow> findByTargetAndMemberStatus(Member target, MemberStatus status);

    List<Follow> findByMemberAndMemberStatus(Member member, MemberStatus status);

    Long countByTarget(Member target);

    Long countByMember(Member member);

    boolean existsByTargetAndMember(Member target, Member member);

    List<Follow> findAllByTargetId(Long senderId);

    @Modifying
    @Query(value = "delete from follow where member_id=:memberId", nativeQuery = true)
    void deleteAllByMemberId(Long memberId);

    @Modifying
    @Query(value = "delete from follow where target_id=:targetId", nativeQuery = true)
    void deleteAllByTargetId(Long targetId);
}
