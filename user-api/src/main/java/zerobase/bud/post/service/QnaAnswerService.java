package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.CHANGE_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_STATUS;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_TYPE_FOR_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_QNA_ANSWER_STATUS;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_QNA_ANSWER;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_GITHUB_USER_ID;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.dto.CreateQnaAnswer.Request;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.QnaAnswerPinRepository;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;
import zerobase.bud.repository.GithubInfoRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class QnaAnswerService {

    private final GithubInfoRepository githubInfoRepository;

    private final PostRepository postRepository;

    private final QnaAnswerRepository qnaAnswerRepository;

    private final QnaAnswerPinRepository qnaAnswerPinRepository;

    @Transactional
    public String createQnaAnswer(String userId, Request request) {
        GithubInfo githubInfo = githubInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_GITHUB_USER_ID));

        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        validateCreateQnaAnswer(post);

        qnaAnswerRepository.save(QnaAnswer.builder()
            .member(githubInfo.getMember())
            .post(post)
            .content(request.getContent())
            .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
            .build());

        post.plusCommentCount();

        return userId;
    }

    @Transactional
    public Long updateQnaAnswer(UpdateQnaAnswer.Request request) {

        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(
                request.getQnaAnswerId())
            .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        validateUpdateQnaAnswer(qnaAnswer, request);

        qnaAnswer.updateContent(request.getContent());

        return request.getQnaAnswerId();
    }

    private void validateUpdateQnaAnswer(QnaAnswer qnaAnswer, UpdateQnaAnswer.Request request) {

        if (!Objects.equals(qnaAnswer.getQnaAnswerStatus(), QnaAnswerStatus.ACTIVE)) {
            throw new BudException(INVALID_QNA_ANSWER_STATUS);
        }

        qnaAnswerPinRepository.findByQnaAnswerId(request.getQnaAnswerId())
            .ifPresent(ap -> {
                throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
            });
    }

    private void validateCreateQnaAnswer(Post post) {
        if (!Objects.equals(post.getPostType(), PostType.QNA)) {
            throw new BudException(INVALID_POST_TYPE_FOR_ANSWER);
        }

        if (!Objects.equals(post.getPostStatus(), PostStatus.ACTIVE)) {
            throw new BudException(INVALID_POST_STATUS);
        }
    }

}