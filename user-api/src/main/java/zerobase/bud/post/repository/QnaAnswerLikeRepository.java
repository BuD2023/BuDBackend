package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.bud.post.domain.QnaAnswerLike;

import java.util.Optional;

public interface QnaAnswerLikeRepository extends JpaRepository<QnaAnswerLike, Long> {
    Optional<QnaAnswerLike> findByQnaAnswerIdAndMemberId(Long qnaAnswerId, Long memberId);
}
