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
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.domain.CommentLike;
import zerobase.bud.comment.domain.CommentPin;
import zerobase.bud.comment.repository.CommentLikeRepository;
import zerobase.bud.comment.repository.CommentPinRepository;
import zerobase.bud.comment.repository.CommentRepository;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.event.AddLikeCommentEvent;
import zerobase.bud.notification.event.CommentPinEvent;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CommentDto;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.type.PostType;
import zerobase.bud.type.MemberStatus;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentPinRepository commentPinRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    Member member = Member.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .status(MemberStatus.VERIFIED)
            .profileImg("abcde.jpg")
            .nickname("안뇽")
            .job("시스템프로그래머")
            .oAuthAccessToken("tokenvalue")
            .build();

    @Test
    @DisplayName("좋아요 성공")
    void successCommentLikeTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
            .member(writer)
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();

        Comment comment = Comment.builder()
                .member(writer)
                .post(post)
                .commentCount(1)
                .likeCount(0)
                .id(3L)
                .build();


        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));

        given(commentLikeRepository.findByCommentAndMember(any(), any()))
                .willReturn(Optional.empty());
        //when
        ArgumentCaptor<CommentLike> commentLikeCaptor = ArgumentCaptor.forClass(CommentLike.class);
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        Long result = commentService.commentLike(123L, member);
        //then
        verify(commentLikeRepository, times(1)).save(commentLikeCaptor.capture());
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        verify(eventPublisher, times(1)).publishEvent(any(AddLikeCommentEvent.class));
        assertEquals(1, commentCaptor.getValue().getLikeCount());
        assertEquals(123L, result);
        assertEquals(1L, commentLikeCaptor.getValue().getMember().getId());
        assertEquals(3L, commentLikeCaptor.getValue().getComment().getId());
    }

    @Test
    @DisplayName("좋아요 취소 성공 - 해당 댓글에 대한 좋아요를 이미 눌렀을 때")
    void successCommentLikeWhenExsitingTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Comment comment = Comment.builder()
                .member(writer)
                .commentCount(1)
                .likeCount(1)
                .id(3L)
                .build();


        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));

        given(commentLikeRepository.findByCommentAndMember(any(), any()))
                .willReturn(Optional.of(CommentLike.builder()
                        .id(12L)
                        .createdAt(LocalDateTime.now())
                        .comment(comment)
                        .member(member)
                        .build()
                ));
        //when
        ArgumentCaptor<CommentLike> commentLikeCaptor = ArgumentCaptor.forClass(CommentLike.class);
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        Long result = commentService.commentLike(123L, member);
        //then
        verify(commentLikeRepository, times(1)).delete(commentLikeCaptor.capture());
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        assertEquals(0, commentCaptor.getValue().getLikeCount());
        assertEquals(123L, result);
        assertEquals(1L, commentLikeCaptor.getValue().getMember().getId());
        assertEquals(3L, commentLikeCaptor.getValue().getComment().getId());
    }

    @Test
    @DisplayName("좋아요 실패 - 대상 댓글 없음")
    void failCommentLikeWhenCommentNotFoundTest() {
        //given
        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.commentLike(123L, member));
        //then
        verify(eventPublisher, times(0)).publishEvent(any(AddLikeCommentEvent.class));
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("댓글 핀 성공")
    void successCommentPinTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .commentCount(1)
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        Long result = commentService.commentPin(12L, writer);
        //then
        verify(commentPinRepository, times(1)).deleteByPost(post);
        verify(commentPinRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any(CommentPinEvent.class));
        assertEquals(12L, result);
    }

    @Test
    @DisplayName("댓글 핀 실패 - 대상 댓글 없음")
    void failCommentPinWhenCommentNotFoundTest() {
        //given
        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.commentPin(123L, member));
        //then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 핀 실패 - 요청한 유저가 게시글 작성자가 아님")
    void failCommentPinWhenNotRequestersPostTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .commentCount(1)
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.commentPin(123L, member));
        //then
        assertEquals(ErrorCode.NOT_POST_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 핀 실패 - 대댓글에 핀 요청")
    void failCommentPinWhenCommentIsRecomment() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        Comment parentComment = Comment.builder()
                .member(writer)
                .post(post)
                .commentCount(3)
                .id(2L)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .parent(parentComment)
                .post(post)
                .commentCount(0)
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.commentPin(123L, member));
        //then
        assertEquals(ErrorCode.CANNOT_PIN_RECOMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 핀 취소 성공")
    void successCancelCommentPinTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.of(post));
        //when
        Long result = commentService.cancelCommentPin(12L, writer);
        //then
        verify(commentPinRepository, times(1)).deleteByPost(post);
        assertEquals(12L, result);
    }

    @Test
    @DisplayName("댓글 핀 취소 실패 - 대상 게시물이 없음")
    void failCancelCommentPinWhenCommentNotFoundTest() {
        //given
        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.cancelCommentPin(123L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 핀 취소 실패 - 요청한 유저가 게시글 작성자가 아님")
    void failCancelCommentPinWhenNotRequestersPostTest() {
        //given
        Member writer = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.of(post));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.cancelCommentPin(123L, member));
        //then
        assertEquals(ErrorCode.NOT_POST_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 조회 성공 - 게시물에 댓글 핀이 존재하지만 페이지가 0임")
    void successCommentsTestWhenPageIs0AndCommentPinIsExisting() {
        //given
        Member postWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Member commentWriter = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("bbbbbbb.jpg")
                .nickname("디비왕")
                .job("데이터베이스관리자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
                .post(post)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        CommentPin commentPin = CommentPin.builder()
                .comment(comment)
                .post(post)
                .build();

        post = Post.builder()
                .id(1L)
                .commentPin(commentPin)
                .member(postWriter)
                .build();

        List<Comment> comments = List.of(
                Comment.builder()
                        .id(1L)
                        .member(member)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다")
                        .build(),
                Comment.builder()
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(commentWriter)
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다2")
                        .build(),
                Comment.builder()
                        .id(4L)
                        .member(postWriter)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(5)
                        .content("여기댓글이요")
                        .build()
        );

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.of(post));

        given(commentLikeRepository.existsByCommentAndAndMember(any(), any()))
                .willReturn(true);

        given(commentRepository
                .findByPostAndParentIsNullAndIdIsNotAndCommentStatus(any(), anyLong(), any(), any()))
                .willReturn(new SliceImpl<>(comments));
        //when
        Slice<CommentDto> dtos = commentService.comments(123L, member, 0, 10);
        //then
        assertEquals(3L, dtos.getContent().get(0).getCommentId());
        assertEquals(true, dtos.getContent().get(0).getIsPinned());
        assertEquals(1L, dtos.getContent().get(1).getCommentId());
        assertEquals(false, dtos.getContent().get(1).getIsPinned());
        assertEquals(true, dtos.getContent().get(1).getIsReader());
        assertEquals(true, dtos.getContent().get(2).getIsReaderLiked());
    }

    @Test
    @DisplayName("댓글 조회 성공 - 게시물에 댓글 핀이 존재하지만 페이지가 0이 아님")
    void successCommentsTestWhenPageIsNot0AndCommentPinIsExisting() {
        //given
        Member postWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Member commentWriter = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("bbbbbbb.jpg")
                .nickname("디비왕")
                .job("데이터베이스관리자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Post post = Post.builder()
                .id(1L)
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
                .post(post)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        CommentPin commentPin = CommentPin.builder()
                .comment(comment)
                .post(post)
                .build();

        post = Post.builder()
                .id(1L)
                .commentPin(commentPin)
                .member(postWriter)
                .build();

        List<Comment> comments = List.of(
                Comment.builder()
                        .id(1L)
                        .member(member)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다")
                        .build(),
                Comment.builder()
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(commentWriter)
                        .commentCount(0)
                        .likeCount(3)
                        .content("댓글을 답니다2")
                        .build(),
                Comment.builder()
                        .id(4L)
                        .member(postWriter)
                        .createdAt(LocalDateTime.now())
                        .commentCount(0)
                        .likeCount(5)
                        .content("여기댓글이요")
                        .build()
        );

        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.of(post));

        given(commentLikeRepository.existsByCommentAndAndMember(any(), any()))
                .willReturn(true);

        given(commentRepository
                .findByPostAndParentIsNullAndIdIsNotAndCommentStatus(any(), anyLong(), any(), any()))
                .willReturn(new SliceImpl<>(comments));
        //when
        Slice<CommentDto> dtos = commentService.comments(123L, member, 1, 10);
        //then
        assertEquals(1L, dtos.getContent().get(0).getCommentId());
        assertEquals(false, dtos.getContent().get(0).getIsPinned());
        assertEquals(true, dtos.getContent().get(0).getIsReader());
        assertEquals(true, dtos.getContent().get(1).getIsReaderLiked());
    }

    @Test
    @DisplayName("댓글 조회 실패 - 해당 게시물 없음")
    void successCommentsTestWhenPostNotFoundTest() {
        //given
        given(postRepository.findByIdAndPostStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.comments(123L, member, 0, 10));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void successDeleteWhenCommentNotFoundTest() {
        //given
        Comment comment = Comment.builder()
                .member(member)
                .createdAt(LocalDateTime.now())
                .commentCount(1)
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        Long result = commentService.delete(123L, member);
        //then
        verify(commentRepository, times(1)).delete(any());
        assertEquals(result, 123L);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 대상 댓글 없음")
    void failDeleteWhenCommentNotFoundTest() {
        //given
        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.delete(123L, member));
        //then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 요청한 유저가 댓글 작성자가 아님")
    void failDeleteWhenNotRequestersPostTest() {
        //given
        Member commentWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .oAuthAccessToken("tokenvalue")
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
                .commentCount(1)
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.delete(123L, member));
        //then
        assertEquals(ErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
    }

}