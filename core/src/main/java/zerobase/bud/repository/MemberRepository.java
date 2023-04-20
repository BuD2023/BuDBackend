package zerobase.bud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(String userId);

    Stream<Member> findAllByIdIn(List<Long> userIds);
}
