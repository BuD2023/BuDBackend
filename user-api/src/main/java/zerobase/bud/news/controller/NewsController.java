package zerobase.bud.news.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.news.dto.DetailNewsResponse;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.service.NewsService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@RestController
@Slf4j
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/news")
    public ResponseEntity<List<SearchAllNews.Response>> getNewsList(
            @Valid SearchAllNews.Request params) {

        return ResponseEntity.ok(newsService.getNewsList(params).stream()
                .map(SearchAllNews.Response::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/news/detail/{newsId}")
    public ResponseEntity<DetailNewsResponse> getNewsDetail(
            @PathVariable("newsId") long id) {

        return ResponseEntity.ok(
                DetailNewsResponse.from(newsService.getNewsDetail(id)));
    }
}