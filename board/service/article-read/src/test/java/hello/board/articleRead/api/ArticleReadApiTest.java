package hello.board.articleRead.api;

import hello.board.articleRead.service.response.ArticleReadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ArticleReadApiTest {
    RestClient restClient = RestClient.create("http://localhost:9005");

    // 이벤트를 받아와서 redis에 저장되면 로그 X, fetch 라는 함수를 타면 로그가 찍힌다.
    @Test
    void readTest() {
        ArticleReadResponse response = restClient.get()
                .uri("/v1/articles/{articleId}", 147550439382646784L)    // DataInitializer 실행 때 생성된 ID
                .retrieve()
                .body(ArticleReadResponse.class);

        System.out.println("response = " + response);
    }
}
