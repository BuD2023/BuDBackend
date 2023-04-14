package zerobase.bud.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
