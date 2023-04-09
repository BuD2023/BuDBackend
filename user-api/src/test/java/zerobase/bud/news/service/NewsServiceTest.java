package zerobase.bud.news.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.repository.NewsRepository;
import zerobase.bud.news.type.NewsSortType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.NEWS_ID_NOT_EXCEED_MIN_VALUE;
import static zerobase.bud.common.type.ErrorCode.NEWS_NOT_FOUND;

@WebMvcTest(MockitoExtension.class)
public class NewsServiceTest {
    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("뉴스 상세 정보 검색 성공")
    void successGetNewsDetail() {
        //given
        given(newsRepository.findById(anyLong()))
                .willReturn(Optional.ofNullable(News.builder()
                        .id(1)
                        .registeredAt(getAnyDateTime(10))
                        .title("기사 제목")
                        .link("http://news/url/0")
                        .summaryContent("요약 본문 입니다.")
                        .content("<div>본문입니다.</div>")
                        .mainImgUrl("http://news/main/img/url/0")
                        .company("네이버뉴스")
                        .journalistOriginalNames("[\"x기x xxxx@naver.com\"," +
                                "\"x희x xxxx@naver.com\"]")
                        .journalistNames("[\"x기x\", \"x희x\"]")
                        .keywords("[\"프론트엔드\",\"백엔드\"]")
                        .hitCount(20)
                        .build()));

        //when
        NewsDto newsDto = newsService.getNewsDetail(1);

        //then
        assertEquals(getAnyDateTime(10), newsDto.getRegisteredAt());
        assertEquals("기사 제목", newsDto.getTitle());
        assertEquals("http://news/url/0", newsDto.getLink());
        assertEquals("요약 본문 입니다.", newsDto.getSummaryContent());
        assertEquals("<div>본문입니다.</div>", newsDto.getContent());
        assertEquals("http://news/main/img/url/0", newsDto.getMainImgUrl());
        assertEquals("네이버뉴스", newsDto.getCompany());
        assertEquals("[\"x기x xxxx@naver.com\",\"x희x xxxx@naver.com\"]"
                , newsDto.getJournalistOriginalNames());
        assertEquals("[\"x기x\", \"x희x\"]", newsDto.getJournalistNames());
        assertEquals("[\"프론트엔드\",\"백엔드\"]", newsDto.getKeywords());
        assertEquals(21, newsDto.getHitCount());

    }

    @Test
    @DisplayName("기사 작성일 기준 내림차순 뉴스 리스트 검색 성공")
    void successGetNewsListNoKeywordOrderByRegisteredAtDesc() {
        //given
        List<News> list = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            News news = News.builder()
                    .id(i)
                    .registeredAt(getAnyDateTime(i))
                    .title("기사 제목")
                    .link("http://news/url/" + i)
                    .summaryContent("본문 요약 내용입니다.")
                    .content("<div>본문입니다.</div>")
                    .mainImgUrl("http://news/main/img/url/" + i)
                    .company("네이버뉴스")
                    .journalistOriginalNames("[\"x기x xxxx@naver.com\", " +
                            "\"x희x xxxx@naver.com\"]")
                    .journalistNames("[\"x기x\", \"x희x\"]")
                    .keywords("[\"프론트엔드\", \"백엔드\"]")
                    .hitCount(20 - i)
                    .build();
            list.add(news);
        }

        int size = 7;
        list.sort(Comparator.comparing(News::getRegisteredAt).reversed());
        list = list.subList(0, size);
        given(newsRepository.findAllByTitleContainingAndRegisteredAtIsBetweenOrderByRegisteredAtDesc(
                any(),
                anyString(),
                any(),
                any()))
                .willReturn(list);

        //when
        List<NewsDto> newsDtoList = newsService.getNewsList(new SearchAllNews.Request(
                "기사",
                size,
                0,
                NewsSortType.DATE,
                getAnyDate(5),
                getAnyDate(20)));

        //then
        assertEquals(7, newsDtoList.size());
        assertEquals("기사 제목", newsDtoList.get(0).getTitle());

        assertEquals("http://news/url/20",
                newsDtoList.get(0).getLink());

        assertEquals("본문 요약 내용입니다.",
                newsDtoList.get(0).getSummaryContent());

        assertEquals("<div>본문입니다.</div>",
                newsDtoList.get(0).getContent());

        assertEquals("http://news/main/img/url/20",
                newsDtoList.get(0).getMainImgUrl());

        assertEquals("네이버뉴스", newsDtoList.get(0).getCompany());

        assertEquals("[\"x기x xxxx@naver.com\", \"x희x xxxx@naver.com\"]"
                , newsDtoList.get(0).getJournalistOriginalNames());

        assertEquals("[\"x기x\", \"x희x\"]",
                newsDtoList.get(0).getJournalistNames());

        assertEquals("[\"프론트엔드\", \"백엔드\"]",
                newsDtoList.get(0).getKeywords());

        assertEquals(0, newsDtoList.get(0).getHitCount());
    }

    @Test
    @DisplayName("뉴스 검색 실패 - 뉴스 고유 아이디 0이하")
    void failGetNewsIdZero(){
        //given
        //when
        BudException budException = assertThrows(BudException.class,
                () -> newsService.getNewsDetail(0));
        //then
        assertEquals(NEWS_ID_NOT_EXCEED_MIN_VALUE, budException.getErrorCode());
    }

    @Test
    @DisplayName("뉴스 검색 실패 - 뉴스 고유 아이디 매칭 데이터 없음")
    void failGetNewsIdNoMatching(){
        //given
        //when
        BudException budException = assertThrows(BudException.class,
                () -> newsService.getNewsDetail(10));
        //then
        assertEquals(NEWS_NOT_FOUND, budException.getErrorCode());
    }

    @Test
    @DisplayName("조회수 내림차순 뉴스 리스트 검색 성공")
    void successGetNewsListNoKeywordOrderByHitDesc() {
        //given
        List<News> list = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            News news = News.builder()
                    .id(i)
                    .registeredAt(getAnyDateTime(i))
                    .title("기사 제목")
                    .link("http://news/url/" + i)
                    .summaryContent("본문 요약 내용입니다.")
                    .content("<div>본문입니다.</div>")
                    .mainImgUrl("http://news/main/img/url/" + i)
                    .company("네이버뉴스")
                    .journalistOriginalNames("[\"x기x xxxx@naver.com\", " +
                            "\"x희x xxxx@naver.com\"]")
                    .journalistNames("[\"x기x\", \"x희x\"]")
                    .keywords("[\"프론트엔드\", \"백엔드\"]")
                    .hitCount(i)
                    .build();
            list.add(news);
        }

        int size = 7;
        list.sort(Comparator.comparing(News::getHitCount).reversed());
        list = list.subList(0, size);
        given(newsRepository.findAllByTitleContainingAndRegisteredAtIsBetweenOrderByHitCountDesc(
                any(),
                anyString(),
                any(),
                any()))
                .willReturn(list);

        //when
        List<NewsDto> newsDtoList = newsService.getNewsList(new SearchAllNews.Request(
                "기사",
                size,
                0,
                NewsSortType.HIT,
                getAnyDate(5),
                getAnyDate(20)));

        //then
        assertEquals(7, newsDtoList.size());
        assertEquals("기사 제목", newsDtoList.get(0).getTitle());

        assertEquals("http://news/url/20",
                newsDtoList.get(0).getLink());

        assertEquals("본문 요약 내용입니다.",
                newsDtoList.get(0).getSummaryContent());

        assertEquals("<div>본문입니다.</div>",
                newsDtoList.get(0).getContent());

        assertEquals("http://news/main/img/url/20",
                newsDtoList.get(0).getMainImgUrl());

        assertEquals("네이버뉴스", newsDtoList.get(0).getCompany());

        assertEquals("[\"x기x xxxx@naver.com\", \"x희x xxxx@naver.com\"]"
                , newsDtoList.get(0).getJournalistOriginalNames());

        assertEquals("[\"x기x\", \"x희x\"]",
                newsDtoList.get(0).getJournalistNames());

        assertEquals("[\"프론트엔드\", \"백엔드\"]",
                newsDtoList.get(0).getKeywords());

        assertEquals(20, newsDtoList.get(0).getHitCount());
    }

    private LocalDateTime getAnyDateTime(int day) {
        return LocalDateTime.of(
                2000, 10,
                day, 13,
                10, 11
        );
    }

    private LocalDate getAnyDate(int day) {
        return LocalDate.parse(LocalDate.of(2000, 10, day)
                .toString(), DateTimeFormatter.ISO_DATE);
    }
}
