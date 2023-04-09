package zerobase.bud.news.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import zerobase.bud.news.type.NewsSortType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SearchAllNews {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        private String keyword;
        @Nullable
        private int size;
        @Nullable
        private int page;
        private NewsSortType sort;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;

        public int getSize() {
            return this.size == 0 ? 10 : this.size;
        }

        public NewsSortType getSort() {
            return this.sort == null ? NewsSortType.HIT : this.sort;
        }

        public boolean ckSort(NewsSortType type) {
            return this.getSort().equals(type);
        }

        public LocalDateTime getStartLocalDateTime() {
            return getStartDate() != null
                    ? getStartDate().atStartOfDay()
                    : LocalDate.MIN.atStartOfDay();
        }

        public LocalDateTime getEndLocalDateTime() {
            return getEndDate() != null
                    ? getEndDate().atStartOfDay().plusDays(1)
                    : LocalDate.now().plusDays(1).atStartOfDay();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private long id;
        private LocalDateTime registeredAt;
        private String title;
        private String link;
        private String summaryContent;
        private String mainImgUrl;
        private String company;
        private String journalistOriginalNames;
        private String journalistNames;
        private String keywords;
        private long hitCount;

        public static Response from(NewsDto newsDto) {
            return Response.builder()
                    .id(newsDto.getId())
                    .registeredAt(newsDto.getRegisteredAt())
                    .title(newsDto.getTitle())
                    .link(newsDto.getLink())
                    .summaryContent(newsDto.getSummaryContent())
                    .mainImgUrl(newsDto.getMainImgUrl())
                    .company(newsDto.getCompany())
                    .journalistOriginalNames(newsDto.getJournalistOriginalNames())
                    .journalistNames(newsDto.getJournalistNames())
                    .keywords(newsDto.getKeywords())
                    .hitCount(newsDto.getHitCount())
                    .build();
        }
    }
}
