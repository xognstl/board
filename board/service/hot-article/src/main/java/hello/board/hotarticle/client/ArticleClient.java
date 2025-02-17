package hello.board.hotarticle.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

/**
 * 게시글 서비스로 게시글 원본 정보 호출
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;
    @Value("${endpoints.hello-board-article-service.url}")
    private String articleServiceUrl;

    @PostConstruct  // 빈이 생성될때 restClient 초기화
    void initRestClient() {
        restClient = RestClient.create(articleServiceUrl);
    }

    // 게시글 조회
    public ArticleResponse read(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
        }catch (Exception e) {
            log.error("[ArticleClient.read] articleId={}", articleId, e);
            return null;
        }
    }

    // 응답 클래스
    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private LocalDateTime createdAt;
    }

}
