package zerobase.bud.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.type.CommentStatus;
import zerobase.bud.post.domain.Post;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndCommentStatus(Long id, CommentStatus commentStatus);

    Slice<Comment> findByPostAndParentIsNullAndIdIsNotAndCommentStatus(Post post, Long pinCommentId, CommentStatus commentStatus, Pageable pageable);

    int countByParent(Comment comment);
}
