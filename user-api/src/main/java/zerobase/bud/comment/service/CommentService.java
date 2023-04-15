package zerobase.bud.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.domain.CommentLike;
import zerobase.bud.comment.domain.CommentPin;
import zerobase.bud.comment.repository.CommentLikeRepository;
import zerobase.bud.comment.repository.CommentPinRepository;
import zerobase.bud.comment.repository.CommentRepository;
import zerobase.bud.comment.type.CommentStatus;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.type.PostStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final CommentLikeRepository commentLikeRepository;

    private final CommentPinRepository commentPinRepository;

    private final PostRepository postRepository;

    public Long commentLike(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndCommentStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if(comment.getMember().equals(member)){
            throw new BudException(ErrorCode.CANNOT_LIKE_WRITER_SELF);
        }

        Optional<CommentLike> optionalCommentLike =
                commentLikeRepository.findByCommentAndMember(comment, member);

        if (optionalCommentLike.isEmpty()) {
            commentLikeRepository.save(CommentLike.builder()
                    .comment(comment)
                    .member(member)
                    .build());
        } else {
            commentLikeRepository.delete(optionalCommentLike.get());
        }

        return commentId;
    }

    @Transactional
    public Long commentPin(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndCommentStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        Post post = comment.getPost();

        if(!post.getMember().equals(member)){
            throw new BudException(ErrorCode.NOT_POST_OWNER);
        }

        commentPinRepository.deleteByPost(post);

        commentPinRepository.save(CommentPin.builder()
                .comment(comment)
                .post(post)
                .build());

        return commentId;
    }

    public Long cancelCommentPin(Long postId, Member member) {

        Post post = postRepository.findByIdAndPostStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_POST));

        if(!post.getMember().equals(member)){
            throw new BudException(ErrorCode.NOT_POST_OWNER);
        }

        commentPinRepository.deleteByPost(post);

        return postId;
    }
}
