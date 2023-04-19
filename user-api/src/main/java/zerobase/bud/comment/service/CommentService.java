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
import zerobase.bud.notification.service.SendNotificationService;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CommentDto;
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

    private final SendNotificationService sendNotificationService;

    @Transactional
    public Long commentLike(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndCommentStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if (Objects.equals(comment.getMember().getId(), member.getId())) {
            throw new BudException(ErrorCode.CANNOT_LIKE_WRITER_SELF);
        }

        Optional<CommentLike> optionalCommentLike = commentLikeRepository.findByCommentAndMember(comment, member);

        if (optionalCommentLike.isPresent()) {
            comment.minusLikeCount();
            commentLikeRepository.delete(optionalCommentLike.get());
        } else {
            comment.addLikeCount();
            commentLikeRepository.save(CommentLike.builder()
                    .comment(comment)
                    .member(member)
                    .build());
            sendNotificationService.sendCommentLikeNotification(member, comment);
        }

        commentRepository.save(comment);
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

        if (!Objects.equals(post.getMember().getId(), member.getId())) {
            throw new BudException(ErrorCode.NOT_POST_OWNER);
        }

        commentPinRepository.deleteByPost(post);
        commentPinRepository.save(CommentPin.builder()
                .comment(comment)
                .post(post)
                .build());

        sendNotificationService.sendCommentPinNotification(member, comment);

        return commentId;
    }

    @Transactional
    public Long cancelCommentPin(Long postId, Member member) {

        Post post = postRepository.findByIdAndPostStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_POST));

        if (!Objects.equals(post.getMember().getId(), member.getId())) {
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
                                        Objects.equals(member.getId(), reComment.getMember().getId()),
                                        commentLikeRepository.existsByCommentAndAndMember(reComment, member))
                        ).collect(Collectors.toList())
        );
    }

    @Transactional
    public Long delete(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndCommentStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if (!Objects.equals(comment.getMember().getId(), member.getId())) {
            throw new BudException(ErrorCode.NOT_COMMENT_OWNER);
        }

        commentRepository.delete(comment);
        return commentId;
    }
}
