package zerobase.bud.news.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.news.domain.News;
import zerobase.bud.news.dto.NewsDto;
import zerobase.bud.news.dto.SearchNaverNewsApi;
import zerobase.bud.news.repository.NewsRepository;

import java.io.IOException;
import java.util.*;

import static zerobase.bud.common.type.ErrorCode.ELEMENT_NOT_EXIST;
import static zerobase.bud.common.type.ErrorCode.URL_ILLEGAL_ARGUMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverNewsApi {
    @Value("${naver-news.client-id}")
    private String naverClientId;
    @Value("${naver-news.client-secret}")
    private String naverClientSecret;

    private final NewsRepository newsRepository;

    @Transactional
    @Scheduled(cron = "0 0 0/1 * * *")
    public void saveNaverNews() {
        String[] keywords = {
                "자바", "자바스크립트", "파이썬",
                "알고리즘", "코딩테스트", "개발", "개발자",
                "인공지능", "안드로이드", "아이폰", "프론트엔드", "백엔드",
                "웹개발", "퍼블리셔", "웹퍼블리셔", "데이터분석",
                "전산", "정보보안", "떠오르는 개발", "C언어"
        };

        String[] sorts = {"date", "sim"};

        for (String keyword : keywords) {
            for (String sort : sorts) {
                SearchNaverNewsApi.Request params = new SearchNaverNewsApi
                        .Request(keyword, 50, 1, sort);

                List<SearchNaverNewsApi.Response> newsFromApi
                        = getNaverNewsFromApi(params);

                for (SearchNaverNewsApi.Response news : newsFromApi) {
                    if (!news.getLink().contains("n.news.naver.com")) {
                        continue;
                    }

                    saveNaverNewsDetail(news, keyword);
                }
            }
        }
    }

    private List<SearchNaverNewsApi.Response> getNaverNewsFromApi(
            SearchNaverNewsApi.Request params) {
        String apiURL = "https://openapi.naver.com/v1/search/news.json?" + params.urlConvert();    // JSON 결과

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", naverClientId);
        requestHeaders.put("X-Naver-Client-Secret", naverClientSecret);
        String responseBody = HttpService.get(apiURL, requestHeaders);

        return parseNaverNewsApi(responseBody);
    }

    private List<SearchNaverNewsApi.Response> parseNaverNewsApi(
            String responseBody) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(responseBody);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JSONArray newsItemsArray = (JSONArray) jsonObject.get("items");

        List<SearchNaverNewsApi.Response> list = new ArrayList<>();

        for (Object o : newsItemsArray) {
            list.add(SearchNaverNewsApi.Response.from((JSONObject) o));
        }

        return list;
    }

    private void saveNaverNewsDetail(SearchNaverNewsApi.Response response,
                                     String keyword) {
        Optional<News> optionalNews
                = newsRepository.findByLink(response.getLink());

        if (optionalNews.isPresent()) {
            addKeyword(optionalNews.get(), keyword);
            return;
        }

        NewsDto newsDto;

        try {
            newsDto = parseNaverNews(response, keyword);;
        } catch (BudException e) {
            //로그
            log.error("{} is occurred ", e.getMessage());
            return;
        }

        newsRepository.save(News.builder()
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
                .build()
        );
    }

    private void addKeyword(News news, String keyword) {
        if (news.getKeywords().contains(keyword)) {
            return;
        }

        Gson gson = new Gson();

        List<String> newsKeywords = gson.fromJson(
                news.getKeywords(),
                new TypeToken<List<String>>() {}.getType()
        );

        newsKeywords.add(keyword);
        news.setKeywords(gson.toJson(newsKeywords));
        newsRepository.save(news);
    }

    private NewsDto parseNaverNews(SearchNaverNewsApi.Response response,
                                   String keyword) throws BudException {
        Document document = getNaverNewsDocument(response.getLink());

        Element content = getElementsExceptSizeZero(document, "div#ct").first();

        Elements journalistNameEle = document.select("span.byline_s");

        List<String> journalistOriginalNames = new ArrayList<>();
        List<String> journalistNames = new ArrayList<>();

        setJournalistNamesAndOriginalNames(
                journalistNameEle,
                journalistOriginalNames,
                journalistNames
        );

        Element companyLogoImg = getElementsExceptSizeZero(content, "img.media_end_head_top_logo_img")
                .first();

        String company = getData(companyLogoImg, "title");

        Element mainImg = content != null
                ? content.select("#img1").first()
                : null;

        String mainImgUrl = mainImg != null
                ? mainImg.attr("data-src")
                : "";

        Element articleEle = getElementsExceptSizeZero(content, "#newsct_article")
                .first();

        Gson gson = new Gson();

        return NewsDto.builder()
                .registeredAt(response.getPubDate())
                .title(response.getTitle())
                .link(response.getLink())
                .summaryContent(response.getDescription())
                .content(articleEle.toString())
                .mainImgUrl(mainImgUrl)
                .company(company)
                .journalistOriginalNames(gson.toJson(journalistOriginalNames))
                .journalistNames(gson.toJson(journalistNames))
                .keywords(gson.toJson(new String[]{keyword}))
                .build();
    }

    private Document getNaverNewsDocument(String link) {
        try {
            return Jsoup.connect(link).get();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BudException(URL_ILLEGAL_ARGUMENT);
        }
    }

    private Elements getElementsExceptSizeZero(Element element,
                                               String cssSelector) {
        validateElementExist(element);
        Elements elements = element.select(cssSelector);
        validateElementExist(elements.first(), cssSelector);

        return element.select(cssSelector);
    }

    private String getData(Element element, String attrKey) {
        validateElementExist(element, attrKey);

        return element.attr(attrKey);
    }

    private void setJournalistNamesAndOriginalNames(Elements journalistNameEle,
                                                    List<String> originals,
                                                    List<String> names) {
        for (Element element : journalistNameEle) {
            String journalistOriginalName = element.text();

            String journalistName = journalistOriginalName.split(" ")[0];

            journalistName = journalistName.split("\\(")[0];

            originals.add(journalistOriginalName);
            names.add(journalistName);
        }
    }

    private void validateElementExist(Element element) {
        if (element == null) {
            throw new BudException(ELEMENT_NOT_EXIST);
        }
    }

    private void validateElementExist(Element element, String key) {
        if (element == null) {
            BudException exception = new BudException(ELEMENT_NOT_EXIST);
            exception.setMessage(exception.getMessage() + key);
            throw exception;
        }
    }
}
