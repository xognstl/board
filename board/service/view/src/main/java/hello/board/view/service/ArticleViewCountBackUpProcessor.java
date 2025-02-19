package hello.board.view.service;

import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleUnikedEventPayload;
import hello.board.common.event.payload.ArticleViewedEventPayload;
import hello.board.common.outboxmessagerelay.OutboxEventPublisher;
import hello.board.view.entity.ArticleViewCount;
import hello.board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void backup(Long articleId, Long viewCount){
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
        if(result == 0){    // 삽입된 레코드가 없을때
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> { },
                            () -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount)));
        }

        /* kafka에 event 발행 */
        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}
