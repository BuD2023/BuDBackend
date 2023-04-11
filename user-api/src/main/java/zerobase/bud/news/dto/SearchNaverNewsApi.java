package zerobase.bud.news.dto;


import lombok.*;
import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SearchNaverNewsApi {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Request {
        private String query;
        private int display;
        private int start;
        private String sort;

        public String urlConvert() {
            String query = this.getQuery() == null ? "IT" : this.getQuery();
            try {
                query = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("검색어 인코딩 실패", e);
            }

            String sort = this.getSort() == null ? "date" : this.getSort();
            int display = this.getDisplay() == 0 ? 100 : this.getDisplay();
            int start = this.getStart() == 0 ? 1 : this.getDisplay();

            StringBuilder sb = new StringBuilder();

            sb.append("query=").append(query)
                    .append("&sort=").append(sort)
                    .append("&display=").append(display)
                    .append("&start=").append(start);

            return sb.toString();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        String title;
        String originalLink;
        String link;
        String description;
        LocalDateTime pubDate;

        public static Response from(JSONObject naverData) {
            return Response.builder()
                    .title(naverData.get("title").toString())
                    .originalLink(naverData.get("originallink").toString())
                    .link(naverData.get("link").toString())
                    .description(naverData.get("description").toString())
                    .pubDate(LocalDateTime.parse(naverData.get("pubDate").toString(),
                            DateTimeFormatter.RFC_1123_DATE_TIME))
                    .build();

        }
    }
}
