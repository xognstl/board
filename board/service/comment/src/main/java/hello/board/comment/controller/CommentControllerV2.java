package hello.board.comment.controller;

import hello.board.comment.service.CommentService;
import hello.board.comment.service.CommentServiceV2;
import hello.board.comment.service.request.CommentCreateRequest;
import hello.board.comment.service.request.CommentCreateRequestV2;
import hello.board.comment.service.response.CommentPageResponse;
import hello.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentControllerV2 {
    private final CommentServiceV2 commentService;

    // 조회
    @GetMapping("/v2/comments/{commentId}")
    public CommentResponse read(@PathVariable Long commentId) {
        return commentService.read(commentId);
    }

    // 생성
    @PostMapping("/v2/comments")
    public CommentResponse create(@RequestBody CommentCreateRequestV2 request) {
        return commentService.create(request);
    }

    // 삭제
    @DeleteMapping("/v2/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
    }

    // 페이지 방식 목록 조회
    @GetMapping("/v2/comments")
    public CommentPageResponse readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAll(articleId, page, pageSize);
    }

    // 무한스크롤 방식 목록 조회
    @GetMapping("/v2/comments/infinite-scroll")
    public List<CommentResponse> readAllInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastPath", required = false) String lastPath,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAllInfiniteScroll(articleId, lastPath, pageSize);
    }

    // total count
    @GetMapping("/v2/comments/articles/{articleId}/count")
    public Long count(@PathVariable Long articleId) {
        return commentService.count(articleId);
    }
}
