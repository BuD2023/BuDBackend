package zerobase.bud.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.domain.CommentLike;
import zerobase.bud.domain.Member;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);
    boolean existsByCommentAndAndMember(Comment comment, Member member);
}
