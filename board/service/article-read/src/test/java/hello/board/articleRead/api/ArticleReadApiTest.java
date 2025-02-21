package hello.board.articleRead.api;

import hello.board.articleRead.service.response.ArticleReadPageResponse;
import hello.board.articleRead.service.response.ArticleReadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ArticleReadApiTest {
    RestClient articleReadRestClient = RestClient.create("http://localhost:9005");
    RestClient articleRestClient = RestClient.create("http://localhost:9000");

    // 이벤트를 받아와서 redis에 저장되면 로그 X, fetch 라는 함수를 타면 로그가 찍힌다.
    @Test
    void readTest() {
        ArticleReadResponse response = articleReadRestClient.get()
                .uri("/v1/articles/{articleId}", 147550439382646784L)    // DataInitializer 실행 때 생성된 ID
                .retrieve()
                .body(ArticleReadResponse.class);

        System.out.println("response = " + response);
    }

    // redis 데이터와 원본 데이터 비교 ( 1페이지 일때는 response1은 redis, response2 는 원본데이터다, 3000페이지 일때는 둘다 원본데이터)
    @Test
    void readAllTest() {
        ArticleReadPageResponse response1 = articleReadRestClient.get()
                .uri("/v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(1L, 3000L, 5))
                .retrieve()
                .body(ArticleReadPageResponse.class);
        System.out.println("response1.getArticleCount = " + response1.getArticleCount());
        for (ArticleReadResponse article : response1.getArticles()) {
            System.out.println("article.getArticleId() = " + article.getArticleId());
        }

        ArticleReadPageResponse response2 = articleRestClient.get()
                .uri("/v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(1L, 3000L, 5))
                .retrieve()
                .body(ArticleReadPageResponse.class);
        System.out.println("response2.getArticleCount = " + response2.getArticleCount());
        for (ArticleReadResponse article : response2.getArticles()) {
            System.out.println("article.getArticleId() = " + article.getArticleId());
        }
    }


    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleReadResponse> responses1 = articleReadRestClient.get()
//                .uri("/v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(1L, 5L))
                .uri("/v1/articles/infinite-scroll?boardId=%s&pageSize=%s&lastArticleId=%s".formatted(1L, 5L, 151172446771970048L))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleReadResponse>>() {
                });
        for (ArticleReadResponse response : responses1) {
            System.out.println("response.getArticleId() = " + response.getArticleId());
        }


        List<ArticleReadResponse> responses2 = articleRestClient.get()
//                .uri("/v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(1L, 5L))
                .uri("/v1/articles/infinite-scroll?boardId=%s&pageSize=%s&lastArticleId=%s".formatted(1L, 5L, 151172446771970048L))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleReadResponse>>() {
                });

        for (ArticleReadResponse response : responses2) {
            System.out.println("response.getArticleId() = " + response.getArticleId());
        }
    }
}
