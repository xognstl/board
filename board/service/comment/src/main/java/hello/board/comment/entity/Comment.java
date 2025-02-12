package hello.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "comment")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId; // shard key
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;    // 상위 댓글 없으면 자기 자신 id
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;    // 생성할땐 false 로
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    public boolean isRoot() {   // 1 depth 댓글인지 확인 유무
        return parentCommentId.longValue() == commentId;
    }

    public void delete() {  // 삭제시 true 로 변경
        deleted = true;
    }
}
