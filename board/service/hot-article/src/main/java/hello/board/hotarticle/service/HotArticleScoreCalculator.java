package hello.board.hotarticle.service;

import hello.board.hotarticle.repository.ArticleCommentCountRepository;
import hello.board.hotarticle.repository.ArticleLikeCountRepository;
import hello.board.hotarticle.repository.ArticleViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 인기글 점수를 계산해주는 class
 * */
@Component
@RequiredArgsConstructor
public class HotArticleScoreCalculator {
    private final ArticleLikeCountRepository articleLikeCountRepository;    // 좋아요 수
    private final ArticleViewCountRepository articleViewCountRepository;    // 조회 수
    private final ArticleCommentCountRepository articleCommentCountRepository;  // 댓글 수

    /* 가중치에 대한 상수 */
    private static final long ARTICLE_LIKE_COUNT_WEIGHT = 3;
    private static final long ARTICLE_COMMENT_COUNT_WEIGHT = 2;
    private static final long ARTICLE_VIEW_COUNT_WEIGHT = 1;

    // 현재 게시글에 대한 점수
    public long calculate(Long articleId) {
        Long articleLikeCount = articleLikeCountRepository.read(articleId);
        Long articleViewCount = articleViewCountRepository.read(articleId);
        Long articleCommentCount = articleCommentCountRepository.read(articleId);

        return articleLikeCount * ARTICLE_LIKE_COUNT_WEIGHT
                + articleViewCount * ARTICLE_VIEW_COUNT_WEIGHT
                + articleCommentCount * ARTICLE_COMMENT_COUNT_WEIGHT;
    }
}
