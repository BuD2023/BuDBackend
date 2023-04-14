package zerobase.bud.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.jwt.TokenProvider;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchAllNews;
import zerobase.bud.news.service.NewsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static zerobase.bud.common.type.ErrorCode.NEWS_NOT_FOUND;

@ExtendWith({RestDocumentationExtension.class})
@WebMvcTest(NewsController.class)
@AutoConfigureRestDocs
public class NewsControllerTest {
    @MockBean
    NewsService newsService;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String token = "token";

    @Test
    @WithMockUser
    @DisplayName("뉴스 리스트 검색 성공")
    void successSearchNews() throws Exception {
        //given
        List<News> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            News news = News.builder()
                    .id(i)
                    .registeredAt(getAnyDate())
                    .title("기사 제목" + i)
                    .link("http://news/url/" + i)
                    .summaryContent("본문 요약 내용입니다." + i)
                    .mainImgUrl("http://news/main/img/url/" + i)
                    .company("네이버뉴스" + i)
                    .journalistOriginalNames("[\"x기x xxxx@naver.com\",\"x희x xxxx@naver.com\"]")
                    .journalistNames("[\"x기x\", \"x희x\"]")
                    .keywords("[\"프론트엔드\", \"백엔드\"]")
                    .hitCount(i)
                    .build();
            list.add(news);
        }

        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<News> newsPage = new PageImpl<>(list, pageRequest, 3);

        given(newsService.getNewsList(any()))
                .willReturn(SearchAllNews.Response.fromEntitesPage(newsPage));

        //when
        //then
        mockMvc.perform(get("/news")
                        .param("size", "3")
                        .param("page", "0")
                        .param("sort", "HIT")
                        .param("order", "ASC")
                        .param("startDate", "2000-01-20")
                        .param("endDate", "2023-03-28")
                        .param("keyword", "제목")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].registeredAt").value(getAnyDate().toString()))
                .andExpect(jsonPath("content[0].title").value("기사 제목1"))
                .andExpect(jsonPath("content[0].link").value("http://news/url/1"))
                .andExpect(jsonPath("content[0].summaryContent").value("본문 요약 내용입니다.1"))
                .andExpect(jsonPath("content[0].mainImgUrl").value("http://news/main/img/url/1"))
                .andExpect(jsonPath("content[0].company").value("네이버뉴스1"))
                .andExpect(jsonPath("content[0].keywords").value("[\"프론트엔드\", \"백엔드\"]"))
                .andExpect(jsonPath("content[0].hitCount").value(1))
                .andExpect(jsonPath("content[0].id").value(1))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedResponseFields(
                                        fieldWithPath("content[].registeredAt").type(JsonFieldType.STRING)
                                                .description("기사 등록일"),
                                        fieldWithPath("content[].title").type(JsonFieldType.STRING)
                                                .description("기사 제목"),
                                        fieldWithPath("content[].link").type(JsonFieldType.STRING)
                                                .description("기사 링크"),
                                        fieldWithPath("content[].summaryContent").type(JsonFieldType.STRING)
                                                .description("기사 요약 내용"),
                                        fieldWithPath("content[].mainImgUrl").type(JsonFieldType.STRING)
                                                .description("기사 메인 이미지 링크"),
                                        fieldWithPath("content[].company").type(JsonFieldType.STRING)
                                                .description("기사 회사명"),
                                        fieldWithPath("content[].journalistOriginalNames").type(JsonFieldType.STRING)
                                                .description("기사 파싱되지 않은 기자(명)들"),
                                        fieldWithPath("content[].journalistNames").type(JsonFieldType.STRING)
                                                .description("기사 파싱된 기자(명)들"),
                                        fieldWithPath("content[].keywords").type(JsonFieldType.STRING)
                                                .description("검색된 키워드들"),
                                        fieldWithPath("content[].hitCount").type(JsonFieldType.NUMBER)
                                                .description("기사 조회수"),
                                        fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                                .description("첫번째 페이지인지 여부"),
                                        fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지인지 여부"),
                                        fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 개수"),
                                        fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 개수"),
                                        fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
                                                .description("검색 데이터 전체 페이지 수"),
                                        fieldWithPath("size").type(JsonFieldType.NUMBER)
                                                .description("요청 데이터 수"),
                                        fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                                                .description("현재 페이지에서 보여지는 데이터 수")
                                )
                        )
                );
    }

    @Test
    @WithMockUser
    @DisplayName("뉴스 상세 정보 검색 성공")
    void successSearchNewsDetail() throws Exception {
        //given
        NewsDto newsDto = NewsDto.builder()
                .id(1)
                .registeredAt(getAnyDate())
                .title("기사 제목")
                .link("http://news/url/0")
                .summaryContent("요약 본문 입니다.")
                .content("<div>본문입니다.</div>")
                .mainImgUrl("http://news/main/img/url/0")
                .company("네이버뉴스")
                .journalistOriginalNames("[\"x기x xxxx@naver.com\",\"x희x xxxx@naver.com\"]")
                .journalistNames("[\"x기x\", \"x희x\"]")
                .keywords("[\"프론트엔드\",\"백엔드\"]")
                .hitCount(20)
                .build();

        given(newsService.getNewsDetail(anyLong()))
                .willReturn(newsDto);

        //when
        //then
        mockMvc.perform(get("/news/detail/1"))
                .andDo(print())
                .andExpect(jsonPath("$.registeredAt").value(getAnyDate().toString()))
                .andExpect(jsonPath("$.title").value("기사 제목"))
                .andExpect(jsonPath("$.link").value("http://news/url/0"))
                .andExpect(jsonPath("$.summaryContent").value("요약 본문 입니다."))
                .andExpect(jsonPath("$.content").value("<div>본문입니다.</div>"))
                .andExpect(jsonPath("$.mainImgUrl").value("http://news/main/img/url/0"))
                .andExpect(jsonPath("$.company").value("네이버뉴스"))
                .andExpect(jsonPath("$.journalistOriginalNames").value("[\"x기x xxxx@naver.com\",\"x희x xxxx@naver.com\"]"))
                .andExpect(jsonPath("$.journalistNames").value("[\"x기x\", \"x희x\"]"))
                .andExpect(jsonPath("$.keywords").value("[\"프론트엔드\",\"백엔드\"]"))
                .andExpect(jsonPath("$.hitCount").value(20))
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }

    @Test
    @WithMockUser
    @DisplayName("뉴스 디테일 데이터 검색 실패")
    void failGetNews() throws Exception {
        //given
        given(newsService.getNewsDetail(anyLong()))
                .willThrow(new BudException(NEWS_NOT_FOUND));

        //when
        //then
        mockMvc.perform(get("/news/detail/876"))
                .andDo(print())
                .andExpect(jsonPath("$.errorCode").value("NEWS_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("요청하신 뉴스 데이터가 존재하지 않습니다."))
                .andExpect(status().isBadRequest())
                .andDo(
                        document("{class-name}/{method-name}",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()))
                );
    }

    private LocalDateTime getAnyDate() {
        return LocalDateTime.of(
                2000, 10,
                10, 13,
                10, 11
        );
    }
}
