package zerobase.bud.post.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.type.QnaAnswerStatus;

import java.util.List;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
    Optional<QnaAnswer> findByIdAndQnaAnswerStatus(Long id, QnaAnswerStatus qnaAnswerStatus);

    List<QnaAnswer> findAllByPostId(Long postId);
}
