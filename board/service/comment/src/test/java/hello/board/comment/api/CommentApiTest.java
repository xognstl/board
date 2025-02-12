package hello.board.comment.api;

import hello.board.comment.service.response.CommentPageResponse;
import hello.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiTest {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my comment2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my comment3", response1.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));


    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 147907627385585664L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v1/comments/{commentId}", 147907628325109760L)
                .retrieve();
         /*테스트 root 댓글 1개 , 대댓글 2개
         1. root 댓글 삭제 -> root 댓글의 delete가 1로 변경
         2. 하위 댓글 삭제 -> DB에서 바로 삭제 된다
         3. 한개남은 하위 댓글을 삭제 -> root, 하위 댓글 같이 삭제*/
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

    }


    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("first page");
        for (CommentResponse comment : response1) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId%s&lastCommentId=%s"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("second page");
        for (CommentResponse comment : response1) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /**
     * 페이지 1번 페이지 수행 결과
     * comment.getCommentId() = 147913451977486336
     * 	comment.getCommentId() = 147913452044595200
     * comment.getCommentId() = 147913451977486337
     * 	comment.getCommentId() = 147913452044595203
     * comment.getCommentId() = 147913451977486338
     * 	comment.getCommentId() = 147913452044595206
     * comment.getCommentId() = 147913451977486339
     * 	comment.getCommentId() = 147913452044595209
     * comment.getCommentId() = 147913451977486340
     * 	comment.getCommentId() = 147913452044595205
     * 	무한페이지
     * 	comment.getCommentId() = 147913451977486336
     * 	comment.getCommentId() = 147913452044595200
     * comment.getCommentId() = 147913451977486337
     * 	comment.getCommentId() = 147913452044595203
     * comment.getCommentId() = 147913451977486338
     * second page
     * comment.getCommentId() = 147913451977486336
     * 	comment.getCommentId() = 147913452044595200
     * comment.getCommentId() = 147913451977486337
     * 	comment.getCommentId() = 147913452044595203
     * comment.getCommentId() = 147913451977486338
     * */

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
