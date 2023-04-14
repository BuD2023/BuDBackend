package zerobase.bud.news.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.service.NewsService;

import javax.validation.Valid;
@RequiredArgsConstructor
@RestController
@Slf4j
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/news")
    public ResponseEntity<Page<SearchAllNews.Response>> getNewsList(
            @Valid SearchAllNews.Request params) {

        return ResponseEntity.ok(newsService.getNewsList(params));
    }

    @GetMapping("/news/detail/{newsId}")
    public ResponseEntity<NewsDto> getNewsDetail(
            @PathVariable("newsId") long id) {

        return ResponseEntity.ok(newsService.getNewsDetail(id));
    }
}