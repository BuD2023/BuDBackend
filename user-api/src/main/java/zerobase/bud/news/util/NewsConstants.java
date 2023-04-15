package zerobase.bud.news.util;

public abstract class NewsConstants {

    public static final String[] NAVER_NEWS_API_KEYWORDS = {
            "자바", "자바스크립트", "파이썬", "알고리즘",
            "코딩테스트", "개발", "개발자", "인공지능",
            "안드로이드", "아이폰", "프론트엔드", "백엔드",
            "퍼블리셔", "데이터분석", "전산", "정보보안",
            "C언어", "코딩", "프로그래밍"
    };
    public static final String[] NAVER_NEWS_API_SORT = {"date", "sim"};

    public static final int RESULT_COUNT = 50;

    public static final String NAVER_NEWS_API_BASIC_URL = "https://openapi.naver.com/v1/search/news.json?";

    public static final String NAVER_NEWS_ITEMS = "items";

    public static final String NAVER_CLIENT_ID_KEY = "X-Naver-Client-Id";

    public static final String NAVER_CLIENT_SECRET_KEY = "X-Naver-Client-Secret";

    public static final String PARSING_POSSIBLE_DOMAIN = "n.news.naver.com";

    public static final String CONTENT_SELECTOR = "div#ct";

    public static final String ARTICLE_SELECTOR = "#newsct_article";

    public static final String COMPANY_TITLE_ATTR_KEY = "title";

    public static final String COMPANY_LOGO_IMG_SELECTOR = "img.media_end_head_top_logo_img";

    public static final String FIRST_IMG_SELECTOR = "#img1";

    public static final String FIRST_IMG_ATTR_KEY = "data-src";

    public static final String JOURNALIST_SELECTOR = "span.byline_s";
}
