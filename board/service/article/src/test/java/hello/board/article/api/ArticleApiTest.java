package hello.board.article.api;

import hello.board.article.service.request.ArticleCreateRequest;
import hello.board.article.service.response.ArticlePageResponse;
import hello.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9000");    // http 요청 할 수 있는 클래스

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "h1", "my content", 1L, 1L
        ));

        System.out.println(response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve() // 호출
                .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(147611126365675520L);   // create 에서 생성된 ID
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(147611126365675520L);
        ArticleResponse response = read(147611126365675520L);
        System.out.println("response = " + response);
    }

    void update(Long articleId) {
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("h2 2", "my content 2222"))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/147611126365675520")
                .retrieve();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize=30&page=50000")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response.getArticleCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("articleId = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> articles1 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });// 리스트로 반환이기 때문에 ParameterizedTypeReference 사용

        System.out.println("first Page");
        for (ArticleResponse articleResponse : articles1) {
            System.out.println("articleId = " + articleResponse.getArticleId());
        }

        Long lastArticleId = articles1.getLast().getArticleId();

        List<ArticleResponse> articles2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        System.out.println("second Page");
        for (ArticleResponse articleResponse : articles2) {
            System.out.println("articleId = " + articleResponse.getArticleId());
        }
    }

    @Test
    void countTest() {
        ArticleResponse response = create(new ArticleCreateRequest("hi", "count", 1L, 2L));

        Long count1 = restClient.get()
                .uri("/v1/articles/boardId/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count = " + count1);

        // 삭제
        restClient.delete()
                .uri("/v1/articles/{articleId}", response.getArticleId())
                .retrieve();

        Long count2 = restClient.get()
                .uri("/v1/articles/boardId/{boardId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count2 = " + count2);
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
