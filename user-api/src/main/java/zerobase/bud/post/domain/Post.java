package zerobase.bud.post.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import zerobase.bud.comment.domain.CommentPin;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.dto.UpdatePost.Request;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private String title;

    private String content;

    private long commentCount;

    private long likeCount;

    private long scrapCount;

    private long hitCount;

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    @OneToOne(mappedBy = "post")
    private CommentPin commentPin;

    @OneToOne(mappedBy = "post")
    private QnaAnswerPin qnaAnswerPin;

    @Builder.Default
    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    public static Post of(Member member, CreatePost.Request request){
        return Post.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .postStatus(PostStatus.ACTIVE)
                .postType(request.getPostType())
                .build();
    }

    public void update(Request request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.postType = request.getPostType();
    }

    public void likeCountUp() {
        this.likeCount++;
    }

    public void likeCountDown() {
        this.likeCount--;
    }

    public void plusCommentCount() {
        this.commentCount += 1;
    }

    public void scrapCountUp() {
        this.scrapCount++;
    }

    public void scrapCountDown() {
        this.scrapCount--;
    }
}

