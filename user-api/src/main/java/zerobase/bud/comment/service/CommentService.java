package zerobase.bud.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
import zerobase.bud.post.dto.CommentDto;
import zerobase.bud.post.dto.RecommentDto;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.type.PostStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final CommentLikeRepository commentLikeRepository;

    private final CommentPinRepository commentPinRepository;

    private final PostRepository postRepository;

    public CommentDto createComment(Long postId, Member member, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_POST));

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(content)
                .likeCount(0)
                .commentCount(0)
                .parent(null)
                .build();
        commentRepository.save(comment);

        return CommentDto.of(comment);
    }

    public CommentDto modifyComment(Long commentId, Member member, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getMember().equals(member)) {
            throw new BudException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.setContent(content);
        commentRepository.save(comment);
        return CommentDto.of(comment);
    }

    public RecommentDto createRecomment(Long commentId, Member member, String content) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        Comment comment = Comment.builder()
                .post(parentComment.getPost())
                .member(member)
                .content(content)
                .likeCount(0)
                .commentCount(0)
                .parent(parentComment)
                .build();

        parentComment.getReComments().add(comment);
        parentComment.setCommentCount(parentComment.getCommentCount() + 1);

        commentRepository.save(parentComment);
        commentRepository.save(comment);

        return RecommentDto.of(comment);
    }

    public Long commentLike(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndCommentStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getMember().equals(member)) {
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

        if (comment.getParent() != null) {
            throw new BudException(ErrorCode.CANNOT_PIN_RECOMMENT);
        }

        Post post = comment.getPost();

        if (!post.getMember().equals(member)) {
            throw new BudException(ErrorCode.NOT_POST_OWNER);
        }

        commentPinRepository.deleteByPost(post);

        commentPinRepository.save(CommentPin.builder()
                .comment(comment)
                .post(post)
                .build());

        return commentId;
    }

    @Transactional
    public Long cancelCommentPin(Long postId, Member member) {

        Post post = postRepository.findByIdAndPostStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_POST));

        if (!post.getMember().equals(member)) {
            throw new BudException(ErrorCode.NOT_POST_OWNER);
        }

        commentPinRepository.deleteByPost(post);

        return postId;
    }

    @Transactional(readOnly = true)
    public Slice<CommentDto> comments(Long postId, Member member, int page, int size) {
        Post post = postRepository.findByIdAndPostStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_POST));

        List<CommentDto> commentDtos = new ArrayList<>();

        Long pinCommentId = -1L;

        if (post.getCommentPin() != null && page == 0) {
            Comment pinComment = post.getCommentPin().getComment();
            pinCommentId = pinComment.getId();
            commentDtos.add(toCommentDto(member, pinComment, true));
        }

        Slice<Comment> comments = commentRepository
                .findByPostAndParentIsNullAndIdIsNotAndCommentStatus(post, pinCommentId,
                        CommentStatus.ACTIVE, PageRequest.of(page, size));

        comments.getContent()
                .forEach(comment -> commentDtos.add(toCommentDto(member, comment, false)));


        return new SliceImpl<>(commentDtos, comments.getPageable(), comments.hasNext());
    }

    private CommentDto toCommentDto(Member member, Comment comment, boolean isPinned) {
        return CommentDto.of(comment,
                Objects.equals(member.getId(), comment.getMember().getId()),
                commentLikeRepository.existsByCommentAndAndMember(comment, member),
                isPinned,
                comment.getReComments().stream()
                        .map(reComment ->
                                CommentDto.of(reComment,
                                        member.equals(reComment.getMember()),
                                        commentLikeRepository.existsByCommentAndAndMember(reComment, member))
                        ).collect(Collectors.toList())
        );
    }

    @Transactional
    public Long delete(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndCommentStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().equals(member)) {
            throw new BudException(ErrorCode.NOT_COMMENT_OWNER);
        }

        commentRepository.delete(comment);

        return commentId;
    }
}
