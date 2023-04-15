package zerobase.bud.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.bud.comment.domain.CommentPin;
import zerobase.bud.post.domain.Post;

public interface CommentPinRepository extends JpaRepository<CommentPin, Long> {
    void deleteByPost(Post post);
}
