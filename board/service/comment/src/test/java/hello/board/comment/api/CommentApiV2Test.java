package hello.board.comment.api;

import hello.board.comment.service.response.CommentPageResponse;
import hello.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());
    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }
    /**
     * response1.getPath() = 00003
     * response1.getCommentId() = 148277722815254528
     * 	response2.getPath() = 0000300000
     * 	response2.getCommentId() = 148277723243073536
     * 		response3.getPath() = 000030000000000
     * 		response3.getCommentId() = 148277723322765312
     * */

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v2/comments/{commentId}", 148277722815254528L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 148277722815254528L)
                .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=50000")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }
    /**
     * response = 101
     * comment.getCommentId() = 148276106770894848
     * comment.getCommentId() = 148276107907551232
     * comment.getCommentId() = 148276108033380352
     * comment.getCommentId() = 148276228825141248
     * comment.getCommentId() = 148276229206822912
     * comment.getCommentId() = 148276229273931776
     * comment.getCommentId() = 148276348354416640
     * comment.getCommentId() = 148276348668989440
     * comment.getCommentId() = 148276348740292608
     * comment.getCommentId() = 148277722815254528
     *
     * response = 500001
     * comment.getCommentId() = 148283682108711002
     * comment.getCommentId() = 148283682108711003
     * comment.getCommentId() = 148283682108711004
     * comment.getCommentId() = 148283682108711005
     * comment.getCommentId() = 148283682108711006
     * comment.getCommentId() = 148283682108711007
     * comment.getCommentId() = 148283682108711008
     * comment.getCommentId() = 148283682108711009
     * comment.getCommentId() = 148283682108711010
     * comment.getCommentId() = 148283682108711011
     *
     * 무한 스크롤
     * first page
     * response.getCommentId() = 148276106770894848
     * response.getCommentId() = 148276107907551232
     * response.getCommentId() = 148276108033380352
     * response.getCommentId() = 148276228825141248
     * response.getCommentId() = 148276229206822912
     * second page
     * response.getCommentId() = 148276229273931776
     * response.getCommentId() = 148276348354416640
     * response.getCommentId() = 148276348668989440
     * response.getCommentId() = 148276348740292608
     * response.getCommentId() = 148277722815254528
     * */

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("first page");
        for (CommentResponse response : response1) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }

        String lastPath = response1.getLast().getPath();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("second page");
        for (CommentResponse response : response2) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
