package hello.board.comment.service;

import hello.board.comment.entity.ArticleCommentCount;
import hello.board.comment.entity.Comment;
import hello.board.comment.entity.CommentPath;
import hello.board.comment.entity.CommentV2;
import hello.board.comment.repository.ArticleCommentCountRepository;
import hello.board.comment.repository.CommentRepositoryV2;
import hello.board.comment.service.request.CommentCreateRequestV2;
import hello.board.comment.service.response.CommentPageResponse;
import hello.board.comment.service.response.CommentResponse;
import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleDeletedEventPayload;
import hello.board.common.event.payload.CommentCreatedEventPayload;
import hello.board.common.event.payload.CommentDeletedEventPayload;
import hello.board.common.outboxmessagerelay.OutboxEventPublisher;
import hello.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public CommentResponse create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request); // 상위 댓글 찾기
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = commentRepository.save(
            CommentV2.create(
                snowflake.nextId(),
                request.getContent(),
                request.getArticleId(),
                request.getWriterId(),
                parentCommentPath.createChildCommentPath(
                        commentRepository.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath())
                                .orElse(null)
                )
            )
        );
        /* total 댓글 수 증가 S */
        int result = articleCommentCountRepository.increase(request.getArticleId());
        if (result == 0) {
            articleCommentCountRepository.save(
                    ArticleCommentCount.init(request.getArticleId(), 1L)
            );
        }
        /* total 댓글 수 증가 E */

        /* kafka에 event 발행 */
        outboxEventPublisher.publish(
                EventType.COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .articleId(comment.getArticleId())
                        .writerId(comment.getWriterId())
                        .deleted(comment.getDeleted())
                        .createdAt(comment.getCreatedAt())
                        .articleCommentCount(count(comment.getArticleId()))
                        .build(),
                comment.getArticleId()
        );

        return CommentResponse.from(comment);
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if(parentPath == null) {
            return null;
        }
        return commentRepository.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted)) // 삭제 확인
                .orElseThrow();
    }

    // 읽기
    public CommentResponse read(Long commentId) {
        return CommentResponse.from(commentRepository.findById(commentId).orElseThrow());
    }

    // 삭제
    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                    /* kafka에 event 발행 */
                    outboxEventPublisher.publish(
                            EventType.COMMENT_DELETED,
                            CommentDeletedEventPayload.builder()
                                    .commentId(comment.getCommentId())
                                    .content(comment.getContent())
                                    .articleId(comment.getArticleId())
                                    .writerId(comment.getWriterId())
                                    .deleted(comment.getDeleted())
                                    .createdAt(comment.getCreatedAt())
                                    .articleCommentCount(count(comment.getArticleId()))
                                    .build(),
                            comment.getArticleId()
                    );
                });

    }

    // descendantsTopPath 를 조회해서 없으면 자손 댓글이 X
    private boolean hasChildren(CommentV2 comment) {
        return commentRepository.findDescendantsTopPath(
                comment.getArticleId(),
                comment.getCommentPath().getPath()
        ).isPresent();
    }

    private void delete(CommentV2 comment) {
        commentRepository.delete(comment);
        /* total 게시글 수 감소 S */
        articleCommentCountRepository.decrease(comment.getArticleId());
        /* total 게시글 수 감소 E */
        if(!comment.isRoot()){  // 삭제된 상위 댓글을 찾아 지워야한다. (상위 댓글이 삭제됬어도 자식이 있어서 못 지워진 경우)
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)    // delete true
                    .filter(not(this::hasChildren)) // 자식이 없어야 지울수 있다.
                    .ifPresent(this::delete);   // 삭제 수행
        }
    }

    // 페이지 번호 방식 메소드
    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
//                count(articleId)
        );
    }

    public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
        List<CommentV2> comments = lastPath == null ?
                commentRepository.findAllInfiniteScroll(articleId, pageSize) :
                commentRepository.findAllInfiniteScroll(articleId, lastPath, pageSize);
        return comments.stream().map(CommentResponse::from).toList();
    }

    public Long count(Long articleId) {
        return articleCommentCountRepository.findById(articleId)
                .map(ArticleCommentCount::getCommentCount)
                .orElse(0L);
    }
}
