package zerobase.bud.post.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import zerobase.bud.comment.domain.QnaAnswerCommentPin;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.QnaAnswerStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class QnaAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    @ManyToOne
    private Member member;

    @NotNull
    private String content;

    private long commentCount;

    private long likeCount;

    @Enumerated(EnumType.STRING)
    private QnaAnswerStatus qnaAnswerStatus;

    @OneToOne(mappedBy = "qnaAnswer")
    private QnaAnswerCommentPin qnaAnswerCommentPin;

    @Builder.Default
    @OneToMany(mappedBy = "qnaAnswer", orphanRemoval = true)
    private List<QnaAnswerImage> qnaAnswerImages = new ArrayList<>();

    public void updateContent(String content) {
        this.content = content;
    }

    public static QnaAnswer of(Member member, Post post, String content) {
        return QnaAnswer.builder()
                .member(member)
                .post(post)
                .content(content)
                .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                .build();
    }

    public void likeCountUp() {
        this.likeCount++;
    }

    public void likeCountDown() {
        this.likeCount--;
    }

    public void commentCountUp() {
        this.commentCount++;
    }

    public void commentCountdown() {
        this.commentCount--;
    }
}
