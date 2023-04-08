package zerobase.bud.news.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import zerobase.bud.domain.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class News  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime registeredAt;
    private String title;
    private String link;
    private String summaryContent;
    @Lob
    private String content;
    private String mainImgUrl;
    private String company;
    private String journalistOriginalNames;
    private String journalistNames;
    private String keywords;
    private long hitCount;
}
