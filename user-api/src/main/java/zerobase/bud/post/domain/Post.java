package zerobase.bud.post.domain;

import static zerobase.bud.post.type.PostStatus.ACTIVE;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.post.dto.CreatePost;
import zerobase.bud.post.dto.UpdatePost;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;

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

    public static Post of(Member member, CreatePost.Request request){
        return Post.builder()
            .member(member)
            .title(request.getTitle())
            .content(request.getContent())
            .postStatus(ACTIVE)
            .postType(request.getPostType())
            .build();
    }

    public void update(UpdatePost.Request request) {
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

