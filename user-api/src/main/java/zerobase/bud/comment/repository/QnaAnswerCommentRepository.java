package zerobase.bud.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.comment.type.QnaAnswerCommentStatus;
import zerobase.bud.post.domain.QnaAnswer;

import java.util.Optional;

@Repository
public interface QnaAnswerCommentRepository extends JpaRepository<QnaAnswerComment, Long> {
    Optional<QnaAnswerComment> findByIdAndQnaAnswerCommentStatus(Long id, QnaAnswerCommentStatus qnaAnswerCommentStatus);

    Slice<QnaAnswerComment> findByQnaAnswerAndParentIsNullAndIdIsNotAndQnaAnswerCommentStatus(QnaAnswer qnaAnswer, Long pinCommentId, QnaAnswerCommentStatus qnaAnswerCommentStatus, Pageable pageable);

}
