package zerobase.bud.news.dto;

import lombok.*;
import zerobase.bud.news.domain.News;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDto {
    private long id;
    private LocalDateTime registeredAt;
    private String title;
    private String link;
    private String summaryContent;
    private String content;
    private String mainImgUrl;
    private String company;
    private String journalistOriginalNames;
    private String journalistNames;
    private String keywords;
    private long hitCount;

    public static NewsDto fromEntity(News news) {
        return NewsDto.builder()
                .id(news.getId())
                .registeredAt(news.getRegisteredAt())
                .title(news.getTitle())
                .link(news.getLink())
                .summaryContent(news.getSummaryContent())
                .content(news.getContent())
                .mainImgUrl(news.getMainImgUrl())
                .company(news.getCompany())
                .journalistOriginalNames(news.getJournalistOriginalNames())
                .journalistNames(news.getJournalistNames())
                .keywords(news.getKeywords())
                .hitCount(news.getHitCount())
                .build();
    }
}
