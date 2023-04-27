package zerobase.bud.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.comment.domain.QnaAnswerCommentLike;
import zerobase.bud.comment.domain.QnaAnswerCommentPin;
import zerobase.bud.comment.repository.QnaAnswerCommentLikeRepository;
import zerobase.bud.comment.repository.QnaAnswerCommentPinRepository;
import zerobase.bud.comment.repository.QnaAnswerCommentRepository;
import zerobase.bud.comment.type.QnaAnswerCommentStatus;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.event.create.CreateAnswerCommentEvent;
import zerobase.bud.notification.event.create.CreateAnswerRecommentEvent;
import zerobase.bud.notification.event.like.AddLikeQnaAnswerCommentEvent;
import zerobase.bud.notification.event.pin.QnaAnswerCommentPinEvent;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.dto.QnaAnswerCommentDto;
import zerobase.bud.post.dto.QnaAnswerRecommentDto;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.type.QnaAnswerStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaAnswerCommentService {

    private final QnaAnswerCommentRepository qnaAnswerCommentRepository;

    private final QnaAnswerCommentLikeRepository qnaAnswerCommentLikeRepository;

    private final QnaAnswerCommentPinRepository qnaAnswerCommentPinRepository;

    private final QnaAnswerRepository qnaAnswerRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long commentLike(Long commentId, Member member) {
        QnaAnswerComment qnaAnswerComment = qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(commentId, QnaAnswerCommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        Optional<QnaAnswerCommentLike> optionalQnaAnswerComment =
                qnaAnswerCommentLikeRepository.findByQnaAnswerCommentAndMember(qnaAnswerComment, member);

        if (optionalQnaAnswerComment.isPresent()) {
            qnaAnswerComment.minusLikeCount();
            qnaAnswerCommentLikeRepository.delete(optionalQnaAnswerComment.get());
        } else {
            qnaAnswerComment.addLikeCount();
            qnaAnswerCommentLikeRepository.save(QnaAnswerCommentLike.builder()
                    .qnaAnswerComment(qnaAnswerComment)
                    .member(member)
                    .build());

            Post post = qnaAnswerComment.getQnaAnswer().getPost();
            eventPublisher.publishEvent(new AddLikeQnaAnswerCommentEvent(
                member, qnaAnswerComment, post.getPostType(), post.getId()
            ));
        }
        qnaAnswerCommentRepository.save(qnaAnswerComment);
        return commentId;
    }

    @Transactional
    public Long commentPin(Long commentId, Member member) {
        QnaAnswerComment qnaAnswerComment = qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(commentId, QnaAnswerCommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if (qnaAnswerComment.getParent() != null) {
            throw new BudException(ErrorCode.CANNOT_PIN_RECOMMENT);
        }

        QnaAnswer qnaAnswer = qnaAnswerComment.getQnaAnswer();

        if (!Objects.equals(qnaAnswer.getMember().getId(), member.getId())) {
            throw new BudException(ErrorCode.NOT_QNA_ANSWER_OWNER);
        }

        qnaAnswerCommentPinRepository.deleteByQnaAnswer(qnaAnswer);
        qnaAnswerCommentPinRepository.save(QnaAnswerCommentPin.builder()
                .qnaAnswerComment(qnaAnswerComment)
                .qnaAnswer(qnaAnswer)
                .build());

        eventPublisher.publishEvent(new QnaAnswerCommentPinEvent(member, qnaAnswerComment));

        return commentId;
    }

    @Transactional
    public Long cancelCommentPin(Long qnaPostId, Member member) {
        QnaAnswer qnaAnswer = qnaAnswerRepository.findByIdAndQnaAnswerStatus(qnaPostId, QnaAnswerStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_QNA_ANSWER));

        if (!Objects.equals(qnaAnswer.getMember().getId(), member.getId())) {
            throw new BudException(ErrorCode.NOT_QNA_ANSWER_OWNER);
        }

        qnaAnswerCommentPinRepository.deleteByQnaAnswer(qnaAnswer);

        return qnaPostId;
    }

    @Transactional(readOnly = true)
    public Slice<QnaAnswerCommentDto> comments(Long qnaPostId, Member member, int page, int size) {
        QnaAnswer qnaAnswer = qnaAnswerRepository.findByIdAndQnaAnswerStatus(qnaPostId, QnaAnswerStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_QNA_ANSWER));

        List<QnaAnswerCommentDto> commentDtos = new ArrayList<>();
        Long pinCommentId = -1L;

        if (qnaAnswer.getQnaAnswerCommentPin() != null && page == 0) {
            QnaAnswerComment pinComment = qnaAnswer.getQnaAnswerCommentPin().getQnaAnswerComment();
            pinCommentId = pinComment.getId();
            commentDtos.add(toQnaCommentDto(member, pinComment, true));
        }

        Slice<QnaAnswerComment> comments = qnaAnswerCommentRepository
                .findByQnaAnswerAndParentIsNullAndIdIsNotAndQnaAnswerCommentStatus(qnaAnswer, pinCommentId,
                        QnaAnswerCommentStatus.ACTIVE, PageRequest.of(page, size));

        comments.getContent()
                .forEach(comment -> commentDtos.add(toQnaCommentDto(member, comment, false)));

        return new SliceImpl<>(commentDtos, comments.getPageable(), comments.hasNext());
    }

    private QnaAnswerCommentDto toQnaCommentDto(Member member, QnaAnswerComment comment, boolean isPinned) {
        return QnaAnswerCommentDto.of(comment,
                Objects.equals(member.getId(), comment.getMember().getId()),
                qnaAnswerCommentLikeRepository.existsByQnaAnswerCommentAndMember(comment, member),
                isPinned,
                comment.getReComments().stream()
                        .map(reComment ->
                                QnaAnswerCommentDto.of(reComment,
                                        Objects.equals(member.getId(), reComment.getMember().getId()),
                                        qnaAnswerCommentLikeRepository.existsByQnaAnswerCommentAndMember(reComment, member))
                        ).collect(Collectors.toList())
        );
    }

    @Transactional
    public Long delete(Long commentId, Member member) {
        QnaAnswerComment qnaAnswerComment = qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(commentId, QnaAnswerCommentStatus.ACTIVE)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if (!Objects.equals(member.getId(), qnaAnswerComment.getMember().getId())) {
            throw new BudException(ErrorCode.NOT_COMMENT_OWNER);
        }

        qnaAnswerCommentRepository.delete(qnaAnswerComment);
        return commentId;
    }


    @Transactional
    public QnaAnswerCommentDto createComment(Long qnaAnswerId, Member member, String content) {
        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(qnaAnswerId)
                .orElseThrow(() -> new BudException(ErrorCode.NOT_FOUND_QNA_ANSWER));

        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .qnaAnswer(qnaAnswer)
                .member(member)
                .content(content)
                .likeCount(0)
                .commentCount(0)
                .parent(null)
                .qnaAnswerCommentStatus(QnaAnswerCommentStatus.ACTIVE)
                .build();

        qnaAnswer.setCommentCount(qnaAnswer.getCommentCount() + 1);

        qnaAnswerCommentRepository.save(qnaAnswerComment);
        qnaAnswerRepository.save(qnaAnswer);

        eventPublisher.publishEvent(new CreateAnswerCommentEvent(member, qnaAnswer));

        return QnaAnswerCommentDto.from(qnaAnswerComment);
    }

    @Transactional
    public QnaAnswerCommentDto modifyComment(Long commentId, Member member, String content) {
        QnaAnswerComment qnaAnswerComment = qnaAnswerCommentRepository.findById(commentId)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(qnaAnswerComment.getMember()
                .getId(), member.getId())) {
            throw new BudException(ErrorCode.NOT_COMMENT_OWNER);
        }

        qnaAnswerComment.setContent(content);
        qnaAnswerCommentRepository.save(qnaAnswerComment);
        return QnaAnswerCommentDto.from(qnaAnswerComment);
    }

    @Transactional
    public QnaAnswerRecommentDto createRecomment(Long commentId, Member member, String content) {
        QnaAnswerComment parentComment = qnaAnswerCommentRepository.findById(commentId)
                .orElseThrow(() -> new BudException(ErrorCode.COMMENT_NOT_FOUND));

        QnaAnswer qnaAnswer = parentComment.getQnaAnswer();
        qnaAnswer.setCommentCount(qnaAnswer.getCommentCount() + 1);

        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .qnaAnswer(qnaAnswer)
                .member(member)
                .content(content)
                .likeCount(0)
                .commentCount(0)
                .parent(parentComment)
                .qnaAnswerCommentStatus(QnaAnswerCommentStatus.ACTIVE)
                .build();

        parentComment.getReComments().add(qnaAnswerComment);
        parentComment.setCommentCount(parentComment.getCommentCount() + 1);


        qnaAnswerCommentRepository.save(parentComment);
        qnaAnswerCommentRepository.save(qnaAnswerComment);
        qnaAnswerRepository.save(qnaAnswer);

        eventPublisher.publishEvent(new CreateAnswerRecommentEvent(member, parentComment));

        return QnaAnswerRecommentDto.of(qnaAnswerComment);
    }
}
