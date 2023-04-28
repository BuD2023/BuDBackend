package zerobase.bud.comment.service;

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
import zerobase.bud.notification.event.like.AddLikeCommentEvent;
import zerobase.bud.notification.event.pin.CommentPinEvent;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.dto.CommentDto;
import zerobase.bud.post.dto.RecommentDto;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.type.PostType;
import zerobase.bud.type.MemberStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.post.type.PostStatus.ACTIVE;

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
            .build();

    @Test
    @DisplayName("댓글 생성 성공")
    void successCreateCommentTest() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(Post.builder()
                        .title("title")
                        .content("content")
                        .postStatus(ACTIVE)
                        .commentCount(0)
                        .postType(PostType.FEED)
                        .build()));

        given(commentRepository.save(any()))
                .willReturn(Comment.builder()
                        .member(member)
                        .likeCount(0)
                        .content("하이하이")
                        .parent(null)
                        .createdAt(LocalDateTime.now())
                        .id(3L)
                        .build());
        //when
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        CommentDto dto = commentService.createComment(123L, member, "이것은 댓글입니다");
        //then
        verify(commentRepository, times(1)).save(commentArgumentCaptor.capture());
        verify(postRepository, times(1)).save(postArgumentCaptor.capture());
        assertEquals("이것은 댓글입니다", commentArgumentCaptor.getValue().getContent());
        assertEquals(0, dto.getNumberOfLikes());
        assertEquals(1, postArgumentCaptor.getValue().getCommentCount());
        assertEquals(1L, dto.getMemberId());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 해당 포스트가 없음")
    void failCreateCommentTestWhenNotFoundPost() {
        //given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.createComment(123L, member, "이것은 댓글입니다"));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void successModifyComment() {
        //given
        Comment comment = Comment.builder()
                .member(member)
                .content("수정전텍스트")
                .likeCount(1)
                .id(3L)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));

        given(commentRepository.save(any()))
                .willReturn(Comment.builder()
                        .member(member)
                        .likeCount(0)
                        .content("수정후 텍스트")
                        .createdAt(LocalDateTime.now())
                        .id(3L)
                        .build());
        //when
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        CommentDto dto = commentService.modifyComment(123L, member, "이것은 댓글입니다");
        //then
        verify(commentRepository, times(1)).save(captor.capture());
        assertEquals("수정후 텍스트", dto.getContent());
        assertEquals("이것은 댓글입니다", captor.getValue().getContent());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 해당 댓글이 없음")
    void failModifyCommentWhenCommentNotFoundTest() {
        //given
        given(commentRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.modifyComment(123L, member, "이것은 댓글입니다"));
        //then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 요청한 유저가 댓글 작성자가 아님")
    void failModifyCommentWhenNotCommentOwnerTest() {
        //given
        Member commentWriter = Member.builder()
                .id(2L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("aaaaaa.jpg")
                .nickname("비가와")
                .job("풀스택개발자")
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
                .content("수정전텍스트")
                .likeCount(1)
                .id(3L)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.modifyComment(123L, member, "이것은 댓글입니다"));
        //then
        assertEquals(ErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 생성 성공")
    void successCreateRecommentTest() {
        //given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .postStatus(ACTIVE)
                .commentCount(0)
                .postType(PostType.FEED)
                .build();

        Comment parentComment = Comment.builder()
                .id(2L)
                .post(post)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(parentComment));

        given(commentRepository.save(any()))
                .willReturn(Comment.builder()
                        .member(member)
                        .likeCount(0)
                        .content("하이하이")
                        .parent(parentComment)
                        .createdAt(LocalDateTime.now())
                        .id(3L)
                        .build());

        //when
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        RecommentDto dto = commentService.createRecomment(123L, member, "이것은 댓글입니다");
        //then
        verify(commentRepository, times(1)).save(commentArgumentCaptor.capture());
        verify(postRepository, times(1)).save(postArgumentCaptor.capture());
        assertEquals("이것은 댓글입니다", commentArgumentCaptor.getValue().getContent());
        assertEquals(0, dto.getNumberOfLikes());
        assertEquals(1, postArgumentCaptor.getValue().getCommentCount());
        assertEquals(1L, dto.getMemberId());
    }

    @Test
    @DisplayName("대댓글 실패 - 원댓글이 없음")
    void failCreateRecommentWhenCommentNotFoundPostTest() {
        //given
        given(commentRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        BudException exception = assertThrows(BudException.class,
                () -> commentService.createRecomment(123L, member, "이것은 대댓글입니다"));
        //then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

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
                .build();

        Comment comment = Comment.builder()
                .member(writer)
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
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
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
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
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
                .build();

        Post post = Post.builder()
                .id(1L)
                .member(writer)
                .build();

        Comment parentComment = Comment.builder()
                .member(writer)
                .post(post)
                .id(2L)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .parent(parentComment)
                .post(post)
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
                .build();

        Member commentWriter = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("bbbbbbb.jpg")
                .nickname("디비왕")
                .job("데이터베이스관리자")
                .build();

        Post post = Post.builder()
                .id(1L)
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
                .post(post)
                .createdAt(LocalDateTime.now())
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
                        .likeCount(3)
                        .content("댓글을 답니다")
                        .build(),
                Comment.builder()
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(commentWriter)
                        .likeCount(3)
                        .content("댓글을 답니다2")
                        .build(),
                Comment.builder()
                        .id(4L)
                        .member(postWriter)
                        .createdAt(LocalDateTime.now())
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
                .build();

        Member commentWriter = Member.builder()
                .id(3L)
                .createdAt(LocalDateTime.now())
                .status(MemberStatus.VERIFIED)
                .profileImg("bbbbbbb.jpg")
                .nickname("디비왕")
                .job("데이터베이스관리자")
                .build();

        Post post = Post.builder()
                .id(1L)
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
                .post(post)
                .createdAt(LocalDateTime.now())
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
                        .likeCount(3)
                        .content("댓글을 답니다")
                        .build(),
                Comment.builder()
                        .id(2L)
                        .createdAt(LocalDateTime.now())
                        .member(commentWriter)
                        .likeCount(3)
                        .content("댓글을 답니다2")
                        .build(),
                Comment.builder()
                        .id(4L)
                        .member(postWriter)
                        .createdAt(LocalDateTime.now())
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
        Post post = Post.builder()
                .id(1L)
                .commentCount(3)
                .likeCount(1)
                .build();

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .createdAt(LocalDateTime.now())
                .id(3L)
                .build();

        given(commentRepository.findByIdAndCommentStatus(anyLong(), any()))
                .willReturn(Optional.of(comment));

        //when
        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        Long result = commentService.delete(123L, member);
        //then
        verify(commentRepository, times(1)).delete(any());
        verify(postRepository, times(1)).save(postArgumentCaptor.capture());
        assertEquals(2, postArgumentCaptor.getValue().getCommentCount());
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
                .build();

        Comment comment = Comment.builder()
                .member(commentWriter)
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