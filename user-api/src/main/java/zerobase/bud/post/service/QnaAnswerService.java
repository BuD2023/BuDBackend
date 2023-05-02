package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.ADD_IMPOSSIBLE_PINNED_ANSWER;
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
import static zerobase.bud.util.Constants.QNA_ANSWERS;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.event.like.AddLikeQnaAnswerEvent;
import zerobase.bud.notification.event.create.CreateQnaAnswerEvent;
import zerobase.bud.notification.event.pin.QnaAnswerPinEvent;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.domain.QnaAnswerImage;
import zerobase.bud.post.domain.QnaAnswerLike;
import zerobase.bud.post.domain.QnaAnswerPin;
import zerobase.bud.post.dto.CreateQnaAnswer.Request;
import zerobase.bud.post.dto.QnaAnswerDto;
import zerobase.bud.post.dto.SearchQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.repository.*;
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

    private final QnaAnswerQuerydsl qnaAnswerRepositoryQuerydsl;

    private final QnaAnswerLikeRepository qnaAnswerLikeRepository;

    private final AwsS3Api awsS3Api;

    private final QnaAnswerImageRepository qnaAnswerImageRepository;

    private final QnaAnswerImageQuerydsl qnaAnswerImageQuerydsl;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String createQnaAnswer(
        List<MultipartFile> images, Request request , Member member
    ) {

        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        validateCreateQnaAnswer(post, member);

        QnaAnswer qnaAnswer =
            qnaAnswerRepository.save(QnaAnswer.of(member, post, request.getContent()));

        saveImages(images, qnaAnswer);

        post.plusCommentCount();

        eventPublisher.publishEvent(new CreateQnaAnswerEvent(member, post));

        return member.getUserId();
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

    @Transactional
    public Long updateQnaAnswer(
        Long qnaAnswerId, List<MultipartFile> images, UpdateQnaAnswer.Request request , Member member
    ) {

        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(qnaAnswerId)
            .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        validateUpdateQnaAnswer(qnaAnswer, member);

        qnaAnswer.updateContent(request.getContent());

        deleteImages(qnaAnswer);

        saveImages(images, qnaAnswer);

        return qnaAnswerId;
    }

    private void validateUpdateQnaAnswer(
        QnaAnswer qnaAnswer
        , Member member
    ) {

        if(!Objects.equals(member.getId(), qnaAnswer.getMember().getId())){
            throw new BudException(NOT_QNA_ANSWER_OWNER);
        }

        if (!Objects.equals(qnaAnswer.getQnaAnswerStatus(), QnaAnswerStatus.ACTIVE)) {
            throw new BudException(INVALID_QNA_ANSWER_STATUS);
        }

        qnaAnswerPinRepository.findByQnaAnswerId(qnaAnswer.getId())
            .ifPresent(ap -> {
                throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
            });
    }

    private void saveImages(List<MultipartFile> images, QnaAnswer qnaAnswer) {
        if (Objects.nonNull(images)) {
            for (MultipartFile image : images) {
                String imagePath = awsS3Api.uploadImage(image, QNA_ANSWERS);
                qnaAnswerImageRepository.save(QnaAnswerImage.of(qnaAnswer, imagePath));
            }
        }
    }

    private void deleteImages(QnaAnswer qnaAnswer) {
        deleteImagesFromS3(qnaAnswer);

        qnaAnswerImageRepository.deleteAllByQnaAnswerId(qnaAnswer.getId());
    }

    private void deleteImagesFromS3(QnaAnswer qnaAnswer) {
        qnaAnswer.getQnaAnswerImages()
                .forEach(qnaAnswerImage -> awsS3Api.deleteImage(qnaAnswerImage.getImagePath()));
    }

    public Page<SearchQnaAnswer.Response> searchQnaAnswers(Member member,
                                                           Long postId,
                                                           Pageable pageable) {
        Page<QnaAnswerDto> qnaAnswerDtos = qnaAnswerRepositoryQuerydsl
                .findAllByPostIdAndQnaAnswerStatusNotLike(member.getId(), postId, pageable);

        return new PageImpl<>(
                qnaAnswerDtos.stream()
                        .map(qnaAnswerDto -> SearchQnaAnswer.Response.of(
                                qnaAnswerDto, qnaAnswerImageQuerydsl
                                        .findImagePathAllByPostId(qnaAnswerDto.getId())))
                        .collect(Collectors.toList()),
                qnaAnswerDtos.getPageable(),
                qnaAnswerDtos.getTotalElements());
    }

    @Transactional
    public void deleteQnaAnswer(Long qnaAnswerId) {
        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(qnaAnswerId)
                .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        Post post = postRepository.findById(qnaAnswer.getPost().getId())
                .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        validateDeleteQnaAnswer(qnaAnswer);

        deleteImagesFromS3(qnaAnswer);

        post.minusCommentCount();

        postRepository.save(post);

        qnaAnswerRepository.deleteByQnaAnswerId(qnaAnswerId);
    }

    private void validateDeleteQnaAnswer(QnaAnswer qnaAnswer) {
        qnaAnswerPinRepository.findByQnaAnswerId(qnaAnswer.getId())
                .ifPresent(ap -> {
                    throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
                });
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

        eventPublisher.publishEvent(new QnaAnswerPinEvent(member, qnaAnswer));

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

    @Transactional
    public boolean addLike(Long qnaAnswerId, Member member) {
        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(qnaAnswerId)
                .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        var isAdd = new AtomicReference<Boolean>(false);

        qnaAnswerLikeRepository.findByQnaAnswerIdAndMemberId(qnaAnswerId, member.getId())
                .ifPresentOrElse(
                        postLike -> cancelLike(postLike, qnaAnswer),
                        () -> isAdd.set(addLike(qnaAnswer, member))
                );

        qnaAnswerRepository.save(qnaAnswer);

        return isAdd.get();
    }

    private void cancelLike(QnaAnswerLike qnaAnswerLike, QnaAnswer qnaAnswer) {
        qnaAnswerLikeRepository.delete(qnaAnswerLike);

        qnaAnswer.likeCountDown();
    }

    private boolean addLike(QnaAnswer qnaAnswer, Member member) {
        qnaAnswerLikeRepository.save(QnaAnswerLike.of(qnaAnswer, member));

        qnaAnswer.likeCountUp();

        eventPublisher.publishEvent(new AddLikeQnaAnswerEvent(member, qnaAnswer));

        return true;
    }

}