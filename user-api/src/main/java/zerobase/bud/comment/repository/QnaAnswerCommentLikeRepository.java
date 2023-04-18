package zerobase.bud.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.comment.domain.QnaAnswerCommentLike;
import zerobase.bud.domain.Member;

import java.util.Optional;

@Repository
public interface QnaAnswerCommentLikeRepository extends JpaRepository<QnaAnswerCommentLike, Long> {
    Optional<QnaAnswerCommentLike> findByQnaAnswerCommentAndMember(QnaAnswerComment qnaAnswerComment, Member member);

    boolean existsByQnaAnswerCommentAndMember(QnaAnswerComment qnaAnswerComment, Member member);
}
