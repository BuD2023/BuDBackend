package zerobase.bud.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.QnaAnswerCommentPin;
import zerobase.bud.post.domain.QnaAnswer;

@Repository
public interface QnaAnswerCommentPinRepository extends JpaRepository<QnaAnswerCommentPin, Long> {
    void deleteByQnaAnswer(QnaAnswer qnaAnswer);
}
