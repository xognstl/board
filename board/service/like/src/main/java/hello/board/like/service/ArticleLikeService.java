package hello.board.like.service;

import hello.board.common.snowflake.Snowflake;
import hello.board.like.entity.ArticleLike;
import hello.board.like.repository.ArticleLikeRepository;
import hello.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeRepository articleLikeRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {  // 사용자가 좋아요 했는지 유무
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    @Transactional
    public void like(Long articleId, Long userId) { // 좋아요 수행
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );
    }   // unique index 떄문에 한건만 데이터 생성

    @Transactional
    public void unlike(Long articleId, Long userId) {   // 좋아요 취소
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLikeRepository::delete);
    }
}
