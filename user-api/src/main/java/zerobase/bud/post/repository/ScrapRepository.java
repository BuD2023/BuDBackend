package zerobase.bud.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.domain.Scrap;

import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByPostIdAndMemberId(Long postId, Long memberId);

    Slice<Scrap> findAllByMemberIdAndPostPostStatus(Pageable pageable,
                                                    Long memberId,
                                                    PostStatus status);
}
