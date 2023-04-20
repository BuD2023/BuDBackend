package zerobase.bud.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import zerobase.bud.comment.domain.QnaAnswerCommentPin;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.QnaAnswerStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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
}
