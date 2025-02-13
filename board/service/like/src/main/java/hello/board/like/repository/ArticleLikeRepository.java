package hello.board.like.repository;

import hello.board.like.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    //articleId, userId 로 데이터 조회
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
