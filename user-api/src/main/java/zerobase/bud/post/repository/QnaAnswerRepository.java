package zerobase.bud.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.post.domain.QnaAnswer;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {

}
