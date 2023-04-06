package zerobase.bud.news.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.News;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.type.NewsSortType;
import zerobase.bud.repository.NewsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static zerobase.bud.common.type.ErrorCode.NEWS_ID_NOT_EXCEED_MIN_VALUE;
import static zerobase.bud.common.type.ErrorCode.NEWS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    @Transactional
    public NewsDto getNewsDetail(long id) {
        News news = getNews(id);
        news.setHitCount(news.getHitCount() + 1);
        newsRepository.save(news);

        return NewsDto.fromEntity(news);
    }

    @Transactional(readOnly = true)
    public List<NewsDto> getNewsList(SearchAllNews.Request request) {
        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        String keyword = request.getKeyword();
        LocalDateTime startDate = request.getStartLocalDateTime();
        LocalDateTime endDate = request.getEndLocalDateTime();

        List<News> newsList;

        if (keyword == null && request.ckSort(NewsSortType.DATE)) {
            newsList = newsRepository.findAllByRegisteredAtIsBetweenOrderByRegisteredAtDesc(
                    pageRequest, startDate, endDate
            );
        } else if (keyword == null && request.ckSort(NewsSortType.HIT)) {
            newsList = newsRepository.findAllByRegisteredAtIsBetweenOrderByHitCountDesc(
                    pageRequest, startDate, endDate
            );
        } else if (request.ckSort(NewsSortType.DATE)) {
            newsList = newsRepository.findAllByTitleNotContainingAndRegisteredAtIsBetweenOrderByRegisteredAtDesc(
                    pageRequest, keyword, startDate, endDate
            );
        } else {
            newsList = newsRepository.findAllByTitleContainingAndRegisteredAtIsBetweenOrderByHitCountDesc(
                    pageRequest, keyword, startDate, endDate
            );
        }

        return newsList.stream()
                .map(NewsDto::fromEntity)
                .collect(Collectors.toList());
    }

    private News getNews(long id) {
        if (id <= 0) {
            throw new BudException(NEWS_ID_NOT_EXCEED_MIN_VALUE);
        }

        return newsRepository.findById(id)
                .orElseThrow(() -> new BudException(NEWS_NOT_FOUND));
    }
}
