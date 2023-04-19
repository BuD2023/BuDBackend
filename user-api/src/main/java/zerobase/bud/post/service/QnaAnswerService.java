package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.ADD_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.ALREADY_DELETE_QNA_ANSWER;
import static zerobase.bud.common.type.ErrorCode.CANNOT_ANSWER_YOURSELF;
import static zerobase.bud.common.type.ErrorCode.CHANGE_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_STATUS;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_TYPE_FOR_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_QNA_ANSWER_STATUS;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_QNA_ANSWER;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_QNA_ANSWER_PIN;
import static zerobase.bud.common.type.ErrorCode.NOT_POST_OWNER;
import static zerobase.bud.common.type.ErrorCode.NOT_QNA_ANSWER_OWNER;

import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.service.SendNotificationService;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.domain.QnaAnswerPin;
import zerobase.bud.post.dto.CreateQnaAnswer.Request;
import zerobase.bud.post.dto.QnaAnswerDto;
import zerobase.bud.post.dto.SearchQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.QnaAnswerPinRepository;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.repository.QnaAnswerRepositoryQuerydslImpl;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class QnaAnswerService {

    private final PostRepository postRepository;

    private final QnaAnswerRepository qnaAnswerRepository;

    private final QnaAnswerPinRepository qnaAnswerPinRepository;

    private final SendNotificationService sendNotificationService;

    private final QnaAnswerRepositoryQuerydslImpl qnaAnswerRepositoryQuerydsl;

    @Transactional
    public String createQnaAnswer(Member member, Request request) {

        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        validateCreateQnaAnswer(post, member);

        qnaAnswerRepository.save(QnaAnswer.of(member, post, request.getContent()));

        post.plusCommentCount();

        sendNotificationService.sendCreateQnaAnswerNotification(member, post);

        return member.getUserId();
    }

    @Transactional
    public Long updateQnaAnswer(UpdateQnaAnswer.Request request , Member member) {

        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(
                request.getQnaAnswerId())
            .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        validateUpdateQnaAnswer(qnaAnswer, request, member);

        qnaAnswer.updateContent(request.getContent());

        return request.getQnaAnswerId();
    }

    private void validateUpdateQnaAnswer(
        QnaAnswer qnaAnswer
        , UpdateQnaAnswer.Request request
        , Member member
    ) {

        if(!Objects.equals(member.getId(), qnaAnswer.getMember().getId())){
            throw new BudException(NOT_QNA_ANSWER_OWNER);
        }

        if (!Objects.equals(qnaAnswer.getQnaAnswerStatus(), QnaAnswerStatus.ACTIVE)) {
            throw new BudException(INVALID_QNA_ANSWER_STATUS);
        }

        qnaAnswerPinRepository.findByQnaAnswerId(request.getQnaAnswerId())
            .ifPresent(ap -> {
                throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
            });
    }

    private void validateCreateQnaAnswer(Post post , Member member) {
        if(Objects.equals(post.getMember().getId(), member.getId())){
            throw new BudException(CANNOT_ANSWER_YOURSELF);
        }

        if (!Objects.equals(post.getPostType(), PostType.QNA)) {
            throw new BudException(INVALID_POST_TYPE_FOR_ANSWER);
        }

        if (!Objects.equals(post.getPostStatus(), PostStatus.ACTIVE)) {
            throw new BudException(INVALID_POST_STATUS);
        }

        if( Objects.nonNull(post.getQnaAnswerPin())){
            throw new BudException(ADD_IMPOSSIBLE_PINNED_ANSWER);
        }
    }

    public Page<SearchQnaAnswer.Response> searchQnaAnswers(Long postId,
                                                     Pageable pageable) {
        Page<QnaAnswerDto> qnaAnswerDtos = qnaAnswerRepositoryQuerydsl
                .findAllByPostIdAndQnaAnswerStatusNotLike(postId, pageable);

        return new PageImpl<>(
                qnaAnswerDtos.stream()
                        .map(SearchQnaAnswer.Response::from)
                        .collect(Collectors.toList()),
                qnaAnswerDtos.getPageable(),
                qnaAnswerDtos.getTotalElements());
    }

    public void deleteQnaAnswer(Long qnaAnswerId) {
        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(qnaAnswerId)
                .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        validateDeleteQnaAnswer(qnaAnswer);

        qnaAnswer.setQnaAnswerStatus(QnaAnswerStatus.INACTIVE);
        qnaAnswerRepository.save(qnaAnswer);
    }

    private void validateDeleteQnaAnswer(QnaAnswer qnaAnswer) {
        qnaAnswerPinRepository.findByQnaAnswerId(qnaAnswer.getId())
                .ifPresent(ap -> {
                    throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
                });

        if (Objects.equals(qnaAnswer.getQnaAnswerStatus(), QnaAnswerStatus.INACTIVE)) {
            throw new BudException(ALREADY_DELETE_QNA_ANSWER);
        }
    }
    @Transactional
    public Long qnaAnswerPin(Long qnaAnswerId, Member member) {

        QnaAnswer qnaAnswer = qnaAnswerRepository.findByIdAndQnaAnswerStatus(
            qnaAnswerId, QnaAnswerStatus.ACTIVE
        ).orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        Post post = qnaAnswer.getPost();

        if(!Objects.equals(post.getMember().getId(), member.getId())){
            throw new BudException(NOT_POST_OWNER);
        }

        if(Objects.nonNull(post.getQnaAnswerPin())){
            throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
        }

        qnaAnswerPinRepository.save(QnaAnswerPin.of(qnaAnswer, post));
        sendNotificationService.sendQnaAnswerPinNotification(member, qnaAnswer);

        return qnaAnswerId;
    }

    @Transactional
    public Long cancelQnaAnswerPin(Long qnaAnswerPinId, Member member) {
        QnaAnswerPin qnaAnswerPin = qnaAnswerPinRepository.findById(
                qnaAnswerPinId)
            .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER_PIN));

        Post post = qnaAnswerPin.getPost();

        if(!Objects.equals(post.getMember().getId(), member.getId())){
            throw new BudException(NOT_POST_OWNER);
        }

        qnaAnswerPinRepository.deleteById(qnaAnswerPinId);

        return qnaAnswerPinId;
    }
}

