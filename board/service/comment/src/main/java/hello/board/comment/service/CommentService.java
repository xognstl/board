package hello.board.comment.service;

import hello.board.comment.entity.Comment;
import hello.board.comment.repository.CommentRepository;
import hello.board.comment.service.request.CommentCreateRequest;
import hello.board.comment.service.response.CommentResponse;
import hello.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getParentCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if(parentCommentId == null){
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))   // 상위 댓글이 삭제된 상태가 아니여야 댓글 작성 가능
                .filter(Comment::isRoot)    // 상위댓글은 root 댓글이여야한다. 최대 2depth 이기 때문
                .orElseThrow();

    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(commentRepository.findById(commentId).orElseThrow());
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> { // 존재 하는 경우
                    if(hasChildren(comment)){   // 대댓글 유무
                        comment.delete();   // delete true 로 변경
                    }else {
                        delete(comment);    // 삭제
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;  // 2개 이면 하위 댓글이 있다.
    }


    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if(!comment.isRoot()){  // 삭제된 상위 댓글을 찾아 지워야한다. (상위 댓글이 삭제됬어도 자식이 있어서 못 지워진 경우)
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)    // delete true
                    .filter(not(this::hasChildren)) // 자식이 없어야 지울수 있다.
                    .ifPresent(this::delete);   // 삭제 수행
        }
    }
}
