package zerobase.bud.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.repository.NewsRepository;
import zerobase.bud.news.repository.NewsRepositoryQuerydsl;
import zerobase.bud.news.type.NewsSortType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.NEWS_ID_NOT_EXCEED_MIN_VALUE;
import static zerobase.bud.common.type.ErrorCode.NEWS_NOT_FOUND;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(MockitoExtension.class)
public class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsRepositoryQuerydsl newsRepositoryQuerydsl;

    @InjectMocks
    private NewsService newsService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
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
    @WithMockUser
    @DisplayName("기사뉴스 리스트 검색 성공")
    void successGetNewsListNoKeywordOrderByRegisteredAtDesc() {
        //given
        List<News> list = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
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

        PageRequest pageRequest = PageRequest.of(0, 3);

        given(newsRepositoryQuerydsl.findAll(any(), anyString(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(list, pageRequest, 3));


        //when
        Page<SearchAllNews.Response> responsePage = newsService.getNewsList(
                new SearchAllNews.Request(
                        "기사",
                        3,
                        0,
                        NewsSortType.HIT,
                        Order.DESC,
                        getAnyDate(1),
                        getAnyDate(3))
        );

        //then
        List<SearchAllNews.Response> responseList
                = responsePage.get().collect(Collectors.toList());

        assertEquals(3, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals("기사 제목",responseList.get(0).getTitle());

        assertEquals("http://news/url/1", responseList.get(0).getLink());

        assertEquals("본문 요약 내용입니다.",
                responseList.get(0).getSummaryContent());

        assertEquals("http://news/main/img/url/1",
                responseList.get(0).getMainImgUrl());

        assertEquals("네이버뉴스", responseList.get(0).getCompany());

        assertEquals("[\"x기x xxxx@naver.com\", \"x희x xxxx@naver.com\"]"
                , responseList.get(0).getJournalistOriginalNames());

        assertEquals("[\"x기x\", \"x희x\"]",
                responseList.get(0).getJournalistNames());

        assertEquals("[\"프론트엔드\", \"백엔드\"]",
                responseList.get(0).getKeywords());

        assertEquals(19, responseList.get(0).getHitCount());
    }

    @Test
    @WithMockUser
    @DisplayName("뉴스 검색 실패 - 뉴스 고유 아이디 0이하")
    void failGetNewsIdZero() {
        //given
        //when
        BudException budException = assertThrows(BudException.class,
                () -> newsService.getNewsDetail(0));
        //then
        assertEquals(NEWS_ID_NOT_EXCEED_MIN_VALUE, budException.getErrorCode());
    }

    @Test
    @WithMockUser
    @DisplayName("뉴스 검색 실패 - 뉴스 고유 아이디 매칭 데이터 없음")
    void failGetNewsIdNoMatching() {
        //given
        //when
        BudException budException = assertThrows(BudException.class,
                () -> newsService.getNewsDetail(10));
        //then
        assertEquals(NEWS_NOT_FOUND, budException.getErrorCode());
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
