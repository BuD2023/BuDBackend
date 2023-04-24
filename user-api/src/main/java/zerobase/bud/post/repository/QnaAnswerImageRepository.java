package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.QnaAnswerImage;

@Repository
public interface QnaAnswerImageRepository extends JpaRepository<QnaAnswerImage, Long> {

    @Modifying
    @Query(value = "delete from qna_answer_image where qna_answer_id=:qnaAnswerId" , nativeQuery = true)
    void deleteAllByQnaAnswerId(Long qnaAnswerId);
}
