package zerobase.bud.news.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailNewsResponse {
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

    public static DetailNewsResponse from(NewsDto newsDto) {
        return DetailNewsResponse.builder()
                .id(newsDto.getId())
                .registeredAt(newsDto.getRegisteredAt())
                .title(newsDto.getTitle())
                .link(newsDto.getLink())
                .summaryContent(newsDto.getSummaryContent())
                .content(newsDto.getContent())
                .mainImgUrl(newsDto.getMainImgUrl())
                .company(newsDto.getCompany())
                .journalistOriginalNames(newsDto.getJournalistOriginalNames())
                .journalistNames(newsDto.getJournalistNames())
                .keywords(newsDto.getKeywords())
                .hitCount(newsDto.getHitCount())
                .build();
    }
}
