package zerobase.bud.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.type.CommentStatus;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndCommentStatus(Long id, CommentStatus commentStatus);

}
