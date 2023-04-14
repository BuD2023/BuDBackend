package zerobase.bud.news.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.repository.NewsRepositoryQuerydsl;
import zerobase.bud.news.repository.NewsRepository;

import static zerobase.bud.common.type.ErrorCode.NEWS_ID_NOT_EXCEED_MIN_VALUE;
import static zerobase.bud.common.type.ErrorCode.NEWS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private final NewsRepositoryQuerydsl newsRepositoryQuerydsl;

    @Transactional
    public NewsDto getNewsDetail(long id) {
        News news = getNews(id);
        news.setHitCount(news.getHitCount() + 1);
        newsRepository.save(news);

        return NewsDto.fromEntity(news);
    }

    @Transactional(readOnly = true)
    public Page<SearchAllNews.Response> getNewsList(SearchAllNews.Request request) {
        return SearchAllNews.Response.fromEntitesPage(
                newsRepositoryQuerydsl.findAll(
                        PageRequest.of(request.getPage(), request.getSize()),
                        request.getKeyword(),
                        request.getSort(),
                        request.getOrder(),
                        request.getStartLocalDateTime(),
                        request.getEndLocalDateTime())
        );
    }

    private News getNews(long id) {
        if (id <= 0) {
            throw new BudException(NEWS_ID_NOT_EXCEED_MIN_VALUE);
        }

        return newsRepository.findById(id)
                .orElseThrow(() -> new BudException(NEWS_NOT_FOUND));
    }
}
