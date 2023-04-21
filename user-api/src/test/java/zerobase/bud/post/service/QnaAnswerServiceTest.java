package zerobase.bud.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.post.type.PostStatus.INACTIVE;
import static zerobase.bud.post.type.PostType.QNA;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.awsS3.AwsS3Api;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.domain.QnaAnswerImage;
import zerobase.bud.post.domain.QnaAnswerLike;
import zerobase.bud.post.domain.QnaAnswerPin;
import zerobase.bud.post.dto.CreateQnaAnswer.Request;
import zerobase.bud.post.dto.QnaAnswerDto;
import zerobase.bud.post.dto.SearchQnaAnswer;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.QnaAnswerImageRepository;
import zerobase.bud.post.repository.QnaAnswerLikeRepository;
import zerobase.bud.post.repository.QnaAnswerPinRepository;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.repository.QnaAnswerRepositoryQuerydslImpl;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;
import zerobase.bud.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class QnaAnswerServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private QnaAnswerRepository qnaAnswerRepository;

    @Mock
    private QnaAnswerLikeRepository qnaAnswerLikeRepository;

    @Mock
    private QnaAnswerPinRepository qnaAnswerPinRepository;

    @Mock
    private QnaAnswerRepositoryQuerydslImpl qnaAnswerRepositoryQuerydsl;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QnaAnswerImageRepository qnaAnswerImageRepository;

    @Mock
    private AwsS3Api awsS3Api;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QnaAnswerService qnaAnswerService;

    @Test
    void success_createQnaAnswer() {
        //given
        List<MultipartFile> images = getMockMultipartFiles();
        given(postRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getPost()));

        given(qnaAnswerRepository.save(any()))
            .willReturn(getQnaAnswer());

        given(awsS3Api.uploadImage(any(), any()))
            .willReturn("awsS3Image");

        given(qnaAnswerImageRepository.save(any()))
            .willReturn(QnaAnswerImage.builder()
                .qnaAnswer(getQnaAnswer())
                .imagePath("imagePath")
                .build());

        ArgumentCaptor<QnaAnswer> captor = ArgumentCaptor.forClass(
            QnaAnswer.class);

        ArgumentCaptor<QnaAnswerImage> imageCaptor = ArgumentCaptor.forClass(
            QnaAnswerImage.class);

        //when
        String answer = qnaAnswerService.createQnaAnswer(
            images
            , Request.builder()
                .postId(1L)
                .content("content")
                .build()
            , getSender());

        //then
        verify(qnaAnswerRepository, times(1)).save(captor.capture());
        verify(qnaAnswerImageRepository, times(1)).save(imageCaptor.capture());
        assertEquals("postContent", captor.getValue().getPost().getContent());
        assertEquals(1, captor.getValue().getPost().getCommentCount());
        assertEquals("content", captor.getValue().getContent());
        assertEquals(QnaAnswerStatus.ACTIVE,
            captor.getValue().getQnaAnswerStatus());
        assertEquals("awsS3Image", imageCaptor.getValue().getImagePath());
        assertEquals("content", imageCaptor.getValue().getQnaAnswer().getContent());
        assertEquals("sender", answer);

    }

    private NotificationInfo getNotificationInfo() {
        return NotificationInfo.builder()
            .member(getSender())
            .fcmToken("fcmToken")
            .isPostPushAvailable(true)
            .isFollowPushAvailable(true)
            .build();
    }

    @Test
    @DisplayName("NOT_FOUND_POST_createQnaAnswer")
    void NOT_FOUND_POST_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        given(postRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(
                getMockMultipartFiles()
               , Request.builder()
                    .postId(1L)
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("CANNOT_ANSWER_YOURSELF_createQnaAnswer")
    void CANNOT_ANSWER_YOURSELF_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        Post post = Post.builder()
            .id(1L)
            .member(getSender())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.of(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(
                    new ArrayList<>()
                    , Request.builder()
                        .postId(1L)
                        .content("content")
                        .build()
                    , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(CANNOT_ANSWER_YOURSELF, budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_POST_TYPE_FOR_ANSWER_createQnaAnswer")
    void INVALID_POST_TYPE_FOR_ANSWER_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        Post post = Post.builder()
            .member(getReceiver())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.of(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(
                new ArrayList<>()
                , Request.builder()
                    .postId(1L)
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(INVALID_POST_TYPE_FOR_ANSWER, budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_POST_STATUS_createQnaAnswer")
    void INVALID_POST_STATUS_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        Post post = Post.builder()
            .member(getReceiver())
            .title("title")
            .content("postContent")
            .postStatus(INACTIVE)
            .postType(QNA)
            .build();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.of(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(
                            new ArrayList<>()
                            , Request.builder()
                                .postId(1L)
                                .content("content")
                                .build()
                            , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(INVALID_POST_STATUS, budException.getErrorCode());
    }

    @Test
    @DisplayName("ADD_IMPOSSIBLE_PINNED_ANSWER_createQnaAnswer")
    void ADD_IMPOSSIBLE_PINNED_ANSWER_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때

        Post post = Post.builder()
            .member(getPostMaker())
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(QNA)
            .qnaAnswerPin(getQnaAnswerPin())
            .build();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.of(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(
                getMockMultipartFiles()
                , Request.builder()
                    .postId(1L)
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(ADD_IMPOSSIBLE_PINNED_ANSWER, budException.getErrorCode());
    }

    @Test
    void success_updateQnaAnswer() {
        //given
        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getQnaAnswer()));

        given(qnaAnswerPinRepository.findByQnaAnswerId(anyLong()))
            .willReturn(Optional.empty());

        //when
        Long answer = qnaAnswerService.updateQnaAnswer(
            3L
            , getMockMultipartFiles()
            , UpdateQnaAnswer.Request.builder()
                .content("content")
                .build()
        , getSender());

        //then
        assertEquals(3L, answer);
    }

    @Test
    @DisplayName("NOT_FOUND_QNA_ANSWER_updateQnaAnswer")
    void NOT_FOUND_QNA_ANSWER_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                1L
                , getMockMultipartFiles()
                , UpdateQnaAnswer.Request.builder()
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_QNA_ANSWER, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_QNA_ANSWER_OWNER_updateQnaAnswer")
    void NOT_QNA_ANSWER_OWNER_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        QnaAnswer qnaAnswer = QnaAnswer.builder()
            .member(getReceiver())
            .post(getPost())
            .content("content")
            .qnaAnswerStatus(QnaAnswerStatus.INACTIVE)
            .build();

        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.of(qnaAnswer));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                1L
                , getMockMultipartFiles()
                , UpdateQnaAnswer.Request.builder()
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(NOT_QNA_ANSWER_OWNER, budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_QNA_ANSWER_STATUS_updateQnaAnswer")
    void INVALID_QNA_ANSWER_STATUS_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        QnaAnswer qnaAnswer = QnaAnswer.builder()
            .member(getSender())
            .post(getPost())
            .content("content")
            .qnaAnswerStatus(QnaAnswerStatus.INACTIVE)
            .build();

        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(qnaAnswer));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                1L
                , getMockMultipartFiles()
                , UpdateQnaAnswer.Request.builder()
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(INVALID_QNA_ANSWER_STATUS, budException.getErrorCode());
    }

    @Test
    @DisplayName("CHANGE_IMPOSSIBLE_PINNED_ANSWER_updateQnaAnswer")
    void CHANGE_IMPOSSIBLE_PINNED_ANSWER_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때

        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getQnaAnswer()));

        given(qnaAnswerPinRepository.findByQnaAnswerId(anyLong()))
            .willReturn(Optional.of(getQnaAnswerPin()));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                1L
                , getMockMultipartFiles()
                , UpdateQnaAnswer.Request.builder()
                    .content("content")
                    .build()
                , getSender()));
        //then 이런 결과가 나온다.
        assertEquals(CHANGE_IMPOSSIBLE_PINNED_ANSWER,
            budException.getErrorCode());
    }

    @Test
    void success_pinnedQnaAnswer() {
        //given
        Post post = Post.builder()
            .member(getPostMaker())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(QNA)
            .build();

        QnaAnswerPin qnaAnswerPin = QnaAnswerPin.builder()
            .id(1L)
            .post(getPost())
            .qnaAnswer(getQnaAnswer())
            .build();

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.ofNullable(QnaAnswer.builder()
                .id(1L)
                .member(getPostMaker())
                .post(post)
                .content("content")
                .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                .build()));

        given(qnaAnswerPinRepository.save(any()))
            .willReturn(qnaAnswerPin);

        ArgumentCaptor<QnaAnswerPin> captor =
            ArgumentCaptor.forClass(QnaAnswerPin.class);

        //when
        Long answer = qnaAnswerService.qnaAnswerPin(1L, getPostMaker());

        //then
        verify(qnaAnswerPinRepository, times(1)).save(captor.capture());
        assertEquals(QNA, captor.getValue().getPost().getPostType());
        assertEquals("content", captor.getValue().getQnaAnswer().getContent());
        assertEquals(1L, answer);
    }

    @Test
    @DisplayName("1)NOT_FOUND_QNA_ANSWER_pinnedQnaAnswer")
    void NOT_FOUND_QNA_ANSWER_pinnedQnaAnswer() {
        //given
        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class, () ->
            qnaAnswerService.qnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_FOUND_QNA_ANSWER, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_POST_OWNER_pinnedQnaAnswer")
    void NOT_POST_OWNER_pinnedQnaAnswer() {
        //given
        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.ofNullable(QnaAnswer.builder()
                .id(1L)
                .post(getPost())
                .member(getPostMaker())
                .content("content")
                .qnaAnswerStatus(QnaAnswerStatus.INACTIVE)
                .build()));

        //when
        BudException budException = assertThrows(BudException.class, () ->
            qnaAnswerService.qnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_POST_OWNER, budException.getErrorCode());
    }

    @Test
    @DisplayName("CHANGE_IMPOSSIBLE_PINNED_ANSWER_pinnedQnaAnswer")
    void CHANGE_IMPOSSIBLE_PINNED_ANSWER_pinnedQnaAnswer() {
        //given
        Post post = Post.builder()
            .member(getPostMaker())
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(QNA)
            .qnaAnswerPin(getQnaAnswerPin())
            .build();

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.ofNullable(QnaAnswer.builder()
                .id(1L)
                .post(post)
                .member(getPostMaker())
                .content("content")
                .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                .build()));

        //when
        BudException budException = assertThrows(BudException.class, () ->
            qnaAnswerService.qnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(CHANGE_IMPOSSIBLE_PINNED_ANSWER, budException.getErrorCode());
    }

    @Test
    void success_cancelQnaAnswerPin() {
        //given
        Post post = Post.builder()
            .member(getPostMaker())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(QNA)
            .build();

        QnaAnswerPin qnaAnswerPin = QnaAnswerPin.builder()
            .id(1L)
            .post(post)
            .qnaAnswer(getQnaAnswer())
            .build();

        given(qnaAnswerPinRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(qnaAnswerPin));

        //when
        Long answer = qnaAnswerService.cancelQnaAnswerPin(1L, getPostMaker());

        //then
        assertEquals(1L, answer);
    }

    @Test
    @DisplayName("NOT_FOUND_QNA_ANSWER_PIN_cancelQnaAnswerPin")
    void NOT_FOUND_QNA_ANSWER_PIN_cancelQnaAnswerPin() {
        //given
        given(qnaAnswerPinRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.cancelQnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_FOUND_QNA_ANSWER_PIN, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_POST_OWNER_cancelQnaAnswerPin")
    void NOT_POST_OWNER_cancelQnaAnswerPin() {
        //given
        Post post = Post.builder()
            .member(getSender())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(QNA)
            .build();

        QnaAnswerPin qnaAnswerPin = QnaAnswerPin.builder()
            .id(1L)
            .post(post)
            .qnaAnswer(getQnaAnswer())
            .build();

        given(qnaAnswerPinRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(qnaAnswerPin));

        //when
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.cancelQnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_POST_OWNER, budException.getErrorCode());
    }

    @Test
    @WithMockUser
    @DisplayName("success_search QnaAnswer list")
    void success_searchQnaAnswers(){
        //given
        List<QnaAnswerDto> list = new ArrayList<>();

        for (long i = 1; i <= 10; i++) {
            list.add(QnaAnswerDto.builder()
                    .id(i)
                    .member(null)
                    .content("내용입니다" + i)
                    .commentCount(i)
                    .likeCount(i)
                    .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .updatedAt(LocalDateTime.now())
                    .pinId(i == 1 ? i : null)
                    .isFollow(true)
                    .isLike(false)
                    .build());
        }

        PageRequest pageable = PageRequest.of(0, 3);
        pageable.withSort(Sort.Direction.DESC, "likeCount");

        given(qnaAnswerRepositoryQuerydsl.findAllByPostIdAndQnaAnswerStatusNotLike(anyLong(), anyLong(), any()))
                .willReturn(new PageImpl<>(list, pageable, 10));

        //when
        Page<SearchQnaAnswer.Response> qnaAnswersPages =
                qnaAnswerService.searchQnaAnswers(Member.builder().id(1L).build(),
                        1L, pageable);

        //then
        assertEquals("내용입니다1", qnaAnswersPages.getContent().get(0).getContent());
        assertEquals(QnaAnswerStatus.ACTIVE, qnaAnswersPages.getContent().get(0).getQnaAnswerStatus());
        assertEquals(1, qnaAnswersPages.getContent().get(0).getCommentCount());
        assertEquals(3, qnaAnswersPages.getContent().get(2).getLikeCount());

        assertTrue(qnaAnswersPages.getContent().get(0).isQnaAnswerPin());
        assertFalse(qnaAnswersPages.getContent().get(1).isQnaAnswerPin());
        assertFalse(qnaAnswersPages.getContent().get(2).isQnaAnswerPin());
        assertTrue(qnaAnswersPages.getContent().get(0).isFollow());
        assertFalse(qnaAnswersPages.getContent().get(0).isLike());
    }

    @Test
    @WithMockUser
    @DisplayName("success delete qnaAnswer")
    void success_deleteQnaAnswer(){
        //given
        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(getQnaAnswer()));

        ArgumentCaptor<QnaAnswer> captor =
                ArgumentCaptor.forClass(QnaAnswer.class);

        //when
        qnaAnswerService.deleteQnaAnswer((long)1);

        //then
        verify(qnaAnswerRepository, times(1))
                .save(captor.capture());

        assertEquals(QnaAnswerStatus.INACTIVE, captor.getValue().getQnaAnswerStatus());
    }

    @Test
    @WithMockUser
    @DisplayName("FAIL NOT_FOUND_QNA_ANSWER")
    void fail_deleteQnaAnswer_QnaAnswerNotFound() {
        //given
        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerService.deleteQnaAnswer(1L));

        //then
        assertEquals(NOT_FOUND_QNA_ANSWER, exception.getErrorCode());
    }

    @Test
    @WithMockUser
    @DisplayName("FAIL CHANGE_IMPOSSIBLE_PINNED_ANSWER")
    void fail_deleteQnaAnswer_QnaAnswerPin() {
        //given
        QnaAnswer qnaAnswer = getQnaAnswer();
        qnaAnswer.setId(1L);

        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.of(qnaAnswer));

        given(qnaAnswerPinRepository.findByQnaAnswerId(anyLong()))
                .willReturn(Optional.of(getQnaAnswerPin()));

        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerService.deleteQnaAnswer(anyLong()));

        //then
        assertEquals(CHANGE_IMPOSSIBLE_PINNED_ANSWER, exception.getErrorCode());
    }

    @Test
    @WithMockUser
    @DisplayName("FAIL ALREADY_DELETE_QNA_ANSWER")
    void fail_deleteQnaAnswer_QnaAnswerAlreadyDelete() {
        //given
        QnaAnswer qnaAnswer = getQnaAnswer();
        qnaAnswer.setQnaAnswerStatus(QnaAnswerStatus.INACTIVE);

        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.of(qnaAnswer));

        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerService.deleteQnaAnswer(1L));

        //then
        assertEquals(ALREADY_DELETE_QNA_ANSWER, exception.getErrorCode());
    }

    @Test
    @DisplayName("성공 - 게시물 좋아요 추가")
    void success_AddQnaAnswerLike(){
        //given
        QnaAnswer qnaAnswer = getQnaAnswer();

        Member member = getReceiver();

        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.of(qnaAnswer));

        given(qnaAnswerLikeRepository.findByQnaAnswerIdAndMemberId(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        ArgumentCaptor<QnaAnswer> qnaAnswerCaptor = ArgumentCaptor.forClass(QnaAnswer.class);

        //when
        boolean setLike = qnaAnswerService.setLike(qnaAnswer.getId(), member);

        //then
        verify(qnaAnswerRepository, times(1)).save(qnaAnswerCaptor.capture());
        assertEquals(1L, qnaAnswer.getId());
        assertEquals(3, qnaAnswerCaptor.getValue().getLikeCount());
        assertTrue(setLike);
    }

    @Test
    @DisplayName("성공 - 게시물 좋아요 해제")
    void success_CancelQnaAnswerLike(){
        //given
        //given
        QnaAnswer qnaAnswer = getQnaAnswer();

        Member member = getReceiver();

        QnaAnswerLike postLike = getQnaAnswerLike();

        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.of(qnaAnswer));

        given(qnaAnswerLikeRepository.findByQnaAnswerIdAndMemberId(anyLong(), anyLong()))
                .willReturn(Optional.of(postLike));

        ArgumentCaptor<QnaAnswer> qnaAnswerCaptor = ArgumentCaptor.forClass(QnaAnswer.class);

        //when
        boolean setLike = qnaAnswerService.setLike(qnaAnswer.getId(), member);

        //then
        verify(qnaAnswerRepository, times(1)).save(qnaAnswerCaptor.capture());
        assertEquals(1L, qnaAnswer.getId());
        assertEquals(1, qnaAnswerCaptor.getValue().getLikeCount());
        assertFalse(setLike);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 게시글 좋아요 ")
    void fail_postLike() {
        //given
        given(qnaAnswerRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
                () -> qnaAnswerService.setLike((long)1, new Member()));

        //then
        assertEquals(NOT_FOUND_QNA_ANSWER, budException.getErrorCode());
    }

    private QnaAnswerPin getQnaAnswerPin() {
        return QnaAnswerPin.builder()
            .post(getPost())
            .qnaAnswer(getQnaAnswer())
            .build();
    }

    private static QnaAnswer getQnaAnswer() {
        return QnaAnswer.builder()
                .id(1L)
                .member(getSender())
                .post(getPost())
                .content("content")
                .likeCount(2)
                .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                .build();
    }

    private static QnaAnswerLike getQnaAnswerLike() {
        return QnaAnswerLike.builder()
                .id(1L)
                .member(getPostMaker())
                .qnaAnswer(getQnaAnswer())
                .build();
    }

    private static Post getPost() {
        return Post.builder()
            .member(getReceiver())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(QNA)
            .build();
    }

    private static Member getPostMaker() {
        return Member.builder()
            .id(3L)
            .nickname("postMaker")
            .userId("postMaker")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static Member getSender() {
        return Member.builder()
            .id(1L)
            .nickname("sender")
            .userId("sender")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static Member getReceiver() {
        return Member.builder()
            .id(2L)
            .nickname("receiver")
            .userId("receiver")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static List<MultipartFile> getMockMultipartFiles() {
        List<MultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("multipartFile",
            "health.jpg",
            "image/jpg",
            "<<jpeg data>>".getBytes()));
        return images;
    }

}