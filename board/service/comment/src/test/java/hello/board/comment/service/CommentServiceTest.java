package hello.board.comment.service;

import hello.board.comment.entity.Comment;
import hello.board.comment.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 단위 테스트로 작성
class CommentServiceTest {
    @InjectMocks
    CommentService commentService; // commentService 생성
    @Mock
    CommentRepository commentRepository; // mocking 객체로 commentRepository 주입

    @Test
    @DisplayName("삭제할 댓글이 자식이 있으면, 삭제 표시만 한다.")
    void deleteShouldMarkDeletedIfHasChildren() {
        //given
        Long articleId = 1L;
        Long commentId = 2L;

        Comment comment = createComment(articleId, commentId);
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));  // findById 의 결과가 모킹 객체일떄
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(2L);

        // when
        commentService.delete(commentId);

        // then
        verify(comment).delete(); // 삭제 표시만 해야함.
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제되지 않은 부모면, 하위 댓글만 삭제한다.")
    void deleteShouldDeletechildOnlyIfNotDeletedParent() {

        //given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);    // 상위 댓글이 있따.
        given(comment.isRoot()).willReturn(false); // 루트 댓글이 아니기 때문에 false 반환

        Comment parentComment = mock(Comment.class);
        given(parentComment.getDeleted()).willReturn(false);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);      // 하위 댓글 삭제
        verify(commentRepository, never()).delete(parentComment);    //상위 댓글은 삭제 내역이 없기 때문에 delete가 호출되지 않는다.
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제된 부모면, 재귀적으로 모두 삭제된다.")
    void deleteShouldDeleteAllRecursivelyIfDeletedParent() {

        //given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = createComment(articleId, parentCommentId);
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);

        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.countBy(articleId, parentCommentId, 2L)).willReturn(1L);

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);
        verify(commentRepository).delete(parentComment);
    }

    // mock 객체 생성
    private Comment createComment(Long articleId, Long commentId) {
        Comment comment = mock(Comment.class);
        // 전달받은 파라미터를 mock 객체에 넣어준다.
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    private Comment createComment(Long articleId, Long commentId, Long parentCommentId) {
        Comment comment = createComment(articleId, commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }
}