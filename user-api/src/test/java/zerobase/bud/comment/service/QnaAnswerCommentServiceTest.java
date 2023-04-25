package zerobase.bud.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.post.type.PostStatus.ACTIVE;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.comment.domain.QnaAnswerCommentLike;
import zerobase.bud.comment.domain.QnaAnswerCommentPin;
import zerobase.bud.comment.repository.QnaAnswerCommentLikeRepository;
import zerobase.bud.comment.repository.QnaAnswerCommentPinRepository;
import zerobase.bud.comment.repository.QnaAnswerCommentRepository;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.event.AddLikeQnaAnswerCommentEvent;
import zerobase.bud.notification.event.QnaAnswerCommentPinEvent;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.dto.QnaAnswerCommentDto;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.type.PostType;
import zerobase.bud.type.MemberStatus;

@ExtendWith(MockitoExtension.class)
class QnaAnswerCommentServiceTest {
    @Mock
    private QnaAnswerCommentRepository qnaAnswerCommentRepository;

    @Mock
    private QnaAnswerCommentLikeRepository qnaAnswerCommentLikeRepository;

    @Mock
    private QnaAnswerCommentPinRepository qnaAnswerCommentPinRepository;

    @Mock
    private QnaAnswerRepository qnaAnswerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QnaAnswerCommentService qnaAnswerCommentService;

    Member member = Member.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .status(MemberStatus.VERIFIED)
            .profileImg("abcde.jpg")
            .nickname("안뇽")
            .job("시스템프로그래머")
            .build();

    @Test
    @DisplayName("qna 댓글 좋아요 성공")
    void successCommentLikeTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        Post post = Post.builder()
            .id(1L)
            .member(writer)
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
            .post(post)
            .build();

        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .member(writer)
                .qnaAnswer(qnaAnswer)
                .commentCount(1)
                .likeCount(0)
                .id(3L)
                .build();


        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswerComment));

        given(qnaAnswerCommentLikeRepository.findByQnaAnswerCommentAndMember(any(), any()))
                .willReturn(Optional.empty());
        //when
        ArgumentCaptor<QnaAnswerCommentLike> commentLikeCaptor = ArgumentCaptor.forClass(QnaAnswerCommentLike.class);
        ArgumentCaptor<QnaAnswerComment> commentCaptor = ArgumentCaptor.forClass(QnaAnswerComment.class);
        Long result = qnaAnswerCommentService.commentLike(123L, member);
        //then
        verify(qnaAnswerCommentLikeRepository, times(1)).save(commentLikeCaptor.capture());
        verify(qnaAnswerCommentRepository, times(1)).save(commentCaptor.capture());
        verify(eventPublisher, times(1)).publishEvent(any(AddLikeQnaAnswerCommentEvent.class));
        assertEquals(1, commentCaptor.getValue().getLikeCount());
        assertEquals(123L, result);
        assertEquals(1L, commentLikeCaptor.getValue().getMember().getId());
        assertEquals(3L, commentLikeCaptor.getValue().getQnaAnswerComment().getId());
    }

    @Test
    @DisplayName("qna 댓글 좋아요 취소 성공 - 해당 댓글에 대한 좋아요를 이미 눌렀을 때")
    void successCommentLikeWhenExsitingTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .member(writer)
                .commentCount(1)
                .likeCount(1)
                .id(3L)
                .build();


        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswerComment));

        given(qnaAnswerCommentLikeRepository.findByQnaAnswerCommentAndMember(any(), any()))
                .willReturn(Optional.of(QnaAnswerCommentLike.builder()
                        .id(12L)
                        .createdAt(LocalDateTime.now())
                        .qnaAnswerComment(qnaAnswerComment)
                        .member(member)
                        .build()
                ));
        //when
        ArgumentCaptor<QnaAnswerCommentLike> commentLikeCaptor = ArgumentCaptor.forClass(QnaAnswerCommentLike.class);
        ArgumentCaptor<QnaAnswerComment> commentCaptor = ArgumentCaptor.forClass(QnaAnswerComment.class);
        Long result = qnaAnswerCommentService.commentLike(123L, member);
        //then
        verify(qnaAnswerCommentLikeRepository, times(1)).delete(commentLikeCaptor.capture());
        verify(qnaAnswerCommentRepository, times(1)).save(commentCaptor.capture());
        verify(eventPublisher, times(0)).publishEvent(any(AddLikeQnaAnswerCommentEvent.class));
        assertEquals(0, commentCaptor.getValue().getLikeCount());
        assertEquals(123L, result);
        assertEquals(1L, commentLikeCaptor.getValue().getMember().getId());
        assertEquals(3L, commentLikeCaptor.getValue().getQnaAnswerComment().getId());
    }

    @Test
    @DisplayName("qna 댓글 좋아요 실패 - 대상 댓글 없음")
    void failCommentLikeWhenCommentNotFoundTest() {
        //given
        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.commentLike(123L, member));
        //then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 핀 성공")
    void successCommentPinTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .member(writer)
                .build();

        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .member(member)
                .qnaAnswer(qnaAnswer)
                .commentCount(1)
                .id(3L)
                .build();

        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswerComment));
        //when
        Long result = qnaAnswerCommentService.commentPin(12L, writer);
        //then
        verify(qnaAnswerCommentPinRepository, times(1)).deleteByQnaAnswer(qnaAnswer);
        verify(qnaAnswerCommentPinRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any(QnaAnswerCommentPinEvent.class));
        assertEquals(12L, result);
    }

    @Test
    @DisplayName("qna 댓글 핀 실패 - 대상 댓글 없음")
    void failCommentPinWhenCommentNotFoundTest() {
        //given
        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.commentPin(123L, member));
        //then
        verify(eventPublisher, times(0)).publishEvent(any(QnaAnswerCommentPinEvent.class));
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 핀 실패 - 요청한 유저가 게시글 작성자가 아님")
    void failCommentPinWhenNotRequestersPostTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .member(writer)
                .build();

        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .member(member)
                .qnaAnswer(qnaAnswer)
                .commentCount(1)
                .id(3L)
                .build();

        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswerComment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.commentPin(123L, member));
        //then
        assertEquals(ErrorCode.NOT_QNA_ANSWER_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 핀 실패 - 대댓글에 핀 요청")
    void failCommentPinWhenCommentIsRecomment() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .member(writer)
                .build();

        QnaAnswerComment parentComment = QnaAnswerComment.builder()
                .member(member)
                .qnaAnswer(qnaAnswer)
                .commentCount(1)
                .id(3L)
                .build();


        QnaAnswerComment qnaAnswerComment = QnaAnswerComment.builder()
                .member(member)
                .parent(parentComment)
                .qnaAnswer(qnaAnswer)
                .commentCount(0)
                .id(3L)
                .build();

        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswerComment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.commentPin(123L, member));
        //then
        assertEquals(ErrorCode.CANNOT_PIN_RECOMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 핀 취소 성공")
    void successCancelCommentPinTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .member(writer)
                .build();

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswer));
        //when
        Long result = qnaAnswerCommentService.cancelCommentPin(12L, writer);
        //then
        verify(qnaAnswerCommentPinRepository, times(1)).deleteByQnaAnswer(qnaAnswer);
        assertEquals(12L, result);
    }

    @Test
    @DisplayName("qna 댓글 핀 취소 실패 - 대상 게시물이 없음")
    void failCancelCommentPinWhenCommentNotFoundTest() {
        //given
        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.cancelCommentPin(123L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_QNA_ANSWER, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 핀 취소 실패 - 요청한 유저가 게시글 작성자가 아님")
    void failCancelCommentPinWhenNotRequestersPostTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .member(writer)
                .build();

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswer));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.cancelCommentPin(123L, member));
        //then
        assertEquals(ErrorCode.NOT_QNA_ANSWER_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 조회 성공 - 게시물에 댓글 핀이 존재하지만 페이지가 0임")
    void successCommentsTestWhenPageIs0AndCommentPinIsExisting() {
        //given
        Member qnaWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        Member commentWriter = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("bbbbbbb.jpg")
                .nickname("디비왕")
                .job("데이터베이스관리자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .build();

        QnaAnswerComment comment = QnaAnswerComment.builder()
                .member(commentWriter)
                .qnaAnswer(qnaAnswer)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        QnaAnswerCommentPin commentPin = QnaAnswerCommentPin.builder()
                .qnaAnswerComment(comment)
                .qnaAnswer(qnaAnswer)
                .build();

        qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .qnaAnswerCommentPin(commentPin)
                .member(qnaWriter)
                .build();

        List<QnaAnswerComment> comments = List.of(
                QnaAnswerComment.builder()
                        .id(1L)
                        .member(member)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다")
                        .build(),
                QnaAnswerComment.builder()
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(commentWriter)
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다2")
                        .build(),
                QnaAnswerComment.builder()
                        .id(4L)
                        .member(qnaWriter)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(5)
                        .content("여기댓글이요")
                        .build()
        );

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswer));

        given(qnaAnswerCommentLikeRepository.existsByQnaAnswerCommentAndMember(any(), any()))
                .willReturn(true);

        given(qnaAnswerCommentRepository
                .findByQnaAnswerAndParentIsNullAndIdIsNotAndQnaAnswerCommentStatus(any(), anyLong(), any(), any()))
                .willReturn(new SliceImpl<>(comments));
        //when
        Slice<QnaAnswerCommentDto> dtos = qnaAnswerCommentService.comments(123L, member, 0, 10);
        //then
        assertEquals(3L, dtos.getContent().get(0).getCommentId());
        assertEquals(true, dtos.getContent().get(0).getIsPinned());
        assertEquals(1L, dtos.getContent().get(1).getCommentId());
        assertEquals(false, dtos.getContent().get(1).getIsPinned());
        assertEquals(true, dtos.getContent().get(1).getIsReader());
        assertEquals(true, dtos.getContent().get(2).getIsReaderLiked());
    }

    @Test
    @DisplayName("qna 댓글 조회 성공 - 게시물에 댓글 핀이 존재하지만 페이지가 0이 아님")
    void successCommentsTestWhenPageIsNot0AndCommentPinIsExisting() {
        //given
        Member qnaWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        Member commentWriter = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("bbbbbbb.jpg")
                .nickname("디비왕")
                .job("데이터베이스관리자")
                .build();

        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .build();

        QnaAnswerComment comment = QnaAnswerComment.builder()
                .member(commentWriter)
                .qnaAnswer(qnaAnswer)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        QnaAnswerCommentPin commentPin = QnaAnswerCommentPin.builder()
                .qnaAnswerComment(comment)
                .qnaAnswer(qnaAnswer)
                .build();

        qnaAnswer = QnaAnswer.builder()
                .id(1L)
                .qnaAnswerCommentPin(commentPin)
                .member(qnaWriter)
                .build();

        List<QnaAnswerComment> comments = List.of(
                QnaAnswerComment.builder()
                        .id(1L)
                        .member(member)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다")
                        .build(),
                QnaAnswerComment.builder()
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(commentWriter)
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다2")
                        .build(),
                QnaAnswerComment.builder()
                        .id(4L)
                        .member(qnaWriter)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(5)
                        .content("여기댓글이요")
                        .build()
        );

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
                .willReturn(Optional.of(qnaAnswer));

        given(qnaAnswerCommentLikeRepository.existsByQnaAnswerCommentAndMember(any(), any()))
                .willReturn(true);

        given(qnaAnswerCommentRepository
                .findByQnaAnswerAndParentIsNullAndIdIsNotAndQnaAnswerCommentStatus(any(), anyLong(), any(), any()))
                .willReturn(new SliceImpl<>(comments));
        //when
        Slice<QnaAnswerCommentDto> dtos = qnaAnswerCommentService.comments(123L, member, 1, 10);
        //then
        assertEquals(1L, dtos.getContent().get(0).getCommentId());
        assertEquals(false, dtos.getContent().get(0).getIsPinned());
        assertEquals(true, dtos.getContent().get(0).getIsReader());
        assertEquals(true, dtos.getContent().get(1).getIsReaderLiked());
    }

    @Test
    @DisplayName("qna 댓글 조회 실패 - 해당 게시물 없음")
    void successCommentsTestWhenPostNotFoundTest() {
        //given
        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.comments(123L, member, 0, 10));
        //then
        assertEquals(ErrorCode.NOT_FOUND_QNA_ANSWER, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 삭제 성공")
    void successDeleteWhenCommentNotFoundTest() {
        //given
        QnaAnswerComment comment = QnaAnswerComment.builder()
                .member(member)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        Long result = qnaAnswerCommentService.delete(123L, member);
        //then
        verify(qnaAnswerCommentRepository, times(1)).delete(any());
        assertEquals(result, 123L);
    }

    @Test
    @DisplayName("qna 댓글 삭제 실패 - 대상 댓글 없음")
    void failDeleteWhenCommentNotFoundTest() {
        //given
        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.delete(123L, member));
        //then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("qna 댓글 삭제 실패 - 요청한 유저가 댓글 작성자가 아님")
    void failDeleteWhenNotRequestersPostTest() {
        //given
        Member commentWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        QnaAnswerComment comment = QnaAnswerComment.builder()
                .member(commentWriter)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        given(qnaAnswerCommentRepository.findByIdAndQnaAnswerCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> qnaAnswerCommentService.delete(123L, member));
        //then
        assertEquals(ErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
    }

}