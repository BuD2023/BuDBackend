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
import static zerobase.bud.common.util.Constants.*;


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
    @Scheduled(cron = "0 0 0/12 * * *")
    public void saveNaverNews() {
        for (String keyword : NAVER_NEWS_API_KEYWORDS) {
            for (String sort : NAVER_NEWS_API_SORT) {
                SearchNaverNewsApi.Request params = new SearchNaverNewsApi
                        .Request(keyword, RESULT_COUNT, 1, sort);

                List<SearchNaverNewsApi.Response> newsFromApi
                        = getNaverNewsFromApi(params);

                for (SearchNaverNewsApi.Response news : newsFromApi) {
                    if (!news.getLink().contains(PARSING_POSSIBLE_DOMAIN)) {
                        continue;
                    }

                    saveNaverNewsDetail(news, keyword);
                }
            }
        }
    }

    private List<SearchNaverNewsApi.Response> getNaverNewsFromApi(
            SearchNaverNewsApi.Request params) {
        String apiURL = NAVER_NEWS_API_BASIC_URL + params.urlConvert();

        Map<String, String> requestHeaders = new HashMap<>();

        requestHeaders.put(NAVER_CLIENT_ID_KEY, naverClientId);
        requestHeaders.put(NAVER_CLIENT_SECRET_KEY, naverClientSecret);

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

        JSONArray newsItemsArray = (JSONArray) jsonObject.get(NAVER_NEWS_ITEMS);

        List<SearchNaverNewsApi.Response> list = new ArrayList<>();

        for (Object o : newsItemsArray) {
            list.add(SearchNaverNewsApi.Response.from((JSONObject) o));
        }

        return list;
    }

    private void saveNaverNewsDetail(SearchNaverNewsApi.Response response,
                                     String keyword) {
        Optional<News> optionalNews = newsRepository.findByLink(response.getLink());

        if (optionalNews.isPresent()) {
            addKeyword(optionalNews.get(), keyword);
            return;
        }

        NewsDto newsDto;

        try {
            newsDto = parseNaverNews(response, keyword);
        } catch (BudException e) {
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
                new TypeToken<List<String>>() {
                }.getType()
        );

        newsKeywords.add(keyword);
        news.setKeywords(gson.toJson(newsKeywords));
        newsRepository.save(news);
    }

    private NewsDto parseNaverNews(SearchNaverNewsApi.Response response,
                                   String keyword) throws BudException {
        Document document = getNaverNewsDocument(response.getLink());

        Element content = getElementsExceptSizeZero(document, CONTENT_SELECTOR)
                .first();

        Elements journalistEle = document.select(JOURNALIST_SELECTOR);

        List<String> journalistOriginalNames = new ArrayList<>();
        List<String> journalistNames = new ArrayList<>();

        setJournalistNamesAndOriginalNames(
                journalistEle,
                journalistOriginalNames,
                journalistNames
        );

        Element companyLogoImg = getElementsExceptSizeZero(content,
                COMPANY_LOGO_IMG_SELECTOR).first();

        String company = getData(companyLogoImg, COMPANY_TITLE_ATTR_KEY);

        Element mainImg = content != null
                ? content.select(FIRST_IMG_SELECTOR).first()
                : null;

        String mainImgUrl = mainImg != null
                ? mainImg.attr(FIRST_IMG_ATTR_KEY)
                : "";

        Element articleEle = getElementsExceptSizeZero(content,
                ARTICLE_SELECTOR).first();

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
            StringBuilder message = new StringBuilder()
                    .append(ELEMENT_NOT_EXIST)
                    .append("(")
                    .append(key)
                    .append(")");

            throw new BudException(ELEMENT_NOT_EXIST, ELEMENT_NOT_EXIST +
                    message.toString());
        }
    }
}
