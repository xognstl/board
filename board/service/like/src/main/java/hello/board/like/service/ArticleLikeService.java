package hello.board.like.service;

import hello.board.common.snowflake.Snowflake;
import hello.board.like.entity.ArticleLike;
import hello.board.like.entity.ArticleLikeCount;
import hello.board.like.repository.ArticleLikeCountRepository;
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
    private final ArticleLikeCountRepository articleLikeCountRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {  // 사용자가 좋아요 했는지 유무
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    // update 구문
    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) { // 좋아요 수행
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        ); // unique index 떄문에 한건만 데이터 생성

        int result = articleLikeCountRepository.increase(articleId);
        System.out.println("result = " + result);
        if(result == 0) {   // update 데이터가 없을 때
            // 최초 요청 시에 update 되는 레코드가 없으므로 , 1로 초기화
            // 트래픽이 순식간에 몰릴 수 있는 상황에서는 유실될 수 있으므로, 게시글 생성 시점에 미리 0으로 초기화 전략도 가능
            articleLikeCountRepository.save(
                    ArticleLikeCount.init(articleId, 1L)
            );
        }

    }

    @Transactional
    public void unlikePessimisticLock1(Long articleId, Long userId) {   // 좋아요 취소
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                            articleLikeRepository.delete(articleLike);
                            articleLikeCountRepository.decrease(articleId);
                });
    }

    // select ... for update + update
    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) { // 좋아요 수행
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );
        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));// 데이터가 없으면 0으로
        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    @Transactional
    public void unlikePessimisticLock2(Long articleId, Long userId) {   // 좋아요 취소
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    // 낙관적 락
    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) { // 좋아요 수행
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    @Transactional
    public void unlikeOptimisticLock(Long articleId, Long userId) {   // 좋아요 취소
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}
