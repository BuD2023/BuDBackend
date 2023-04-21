package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.QnaAnswerImage;

@Repository
public interface QnaAnswerImageRepository extends JpaRepository<QnaAnswerImage, Long> {
    void deleteAllByQnaAnswerId(Long qnaAnswerId);
}
