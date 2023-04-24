package zerobase.bud.post.domain;

import javax.persistence.Entity;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class QnaAnswerImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private QnaAnswer qnaAnswer;

    private String imagePath;

    public static QnaAnswerImage of(QnaAnswer qnaAnswer, String imagePath) {
        return QnaAnswerImage.builder()
            .qnaAnswer(qnaAnswer)
            .imagePath(imagePath)
            .build();
    }
}
