package zerobase.bud.post.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.QnaAnswerPin;

@Repository
public interface QnaAnswerPinRepository extends
    JpaRepository<QnaAnswerPin, Long> {

    Optional<QnaAnswerPin> findByQnaAnswerId(Long qnaAnswerId);

    void deleteByPostId(Long postId);

}
