package zerobase.bud.comment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.domain.CommentLike;
import zerobase.bud.comment.repository.CommentLikeRepository;
import zerobase.bud.comment.repository.CommentPinRepository;
import zerobase.bud.comment.repository.CommentRepository;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.common.type.ErrorCode;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.type.MemberStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        Comment comment = Comment.builder()
                .member(writer)
                .commentCount(1)
                .id(3L)
                .build();


        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));

        given(commentLikeRepository.findByCommentAndMember(any(), any()))
                .willReturn(Optional.empty());
        //when
        ArgumentCaptor<CommentLike> captor = ArgumentCaptor.forClass(CommentLike.class);
        Long result = commentService.commentLike(123L, member);
        //then
        verify(commentLikeRepository, times(1)).save(captor.capture());
        assertEquals(123L, result);
        assertEquals(1L, captor.getValue().getMember().getId());
        assertEquals(3L, captor.getValue().getComment().getId());
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
        ArgumentCaptor<CommentLike> captor = ArgumentCaptor.forClass(CommentLike.class);
        Long result = commentService.commentLike(123L, member);
        //then
        verify(commentLikeRepository, times(1)).delete(captor.capture());
        assertEquals(123L, result);
        assertEquals(1L, captor.getValue().getMember().getId());
        assertEquals(3L, captor.getValue().getComment().getId());
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
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 실패 - 자신의 댓글을 좋아요")
    void failCommentLikeWhenRequesterIsWriterTest() {
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
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.commentLike(123L, writer));
        //then
        assertEquals(ErrorCode.CANNOT_LIKE_WRITER_SELF, exception.getErrorCode());
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

}