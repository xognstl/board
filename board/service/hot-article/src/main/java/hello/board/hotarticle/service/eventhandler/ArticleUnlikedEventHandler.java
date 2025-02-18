package hello.board.hotarticle.service.eventhandler;

import hello.board.common.event.Event;
import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleUnikedEventPayload;
import hello.board.hotarticle.repository.ArticleLikeCountRepository;
import hello.board.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUnlikedEventHandler implements EventHandler<ArticleUnikedEventPayload>{
    private final ArticleLikeCountRepository articleLikeCountRepository;

    @Override
    public void handle(Event<ArticleUnikedEventPayload> event) {
        ArticleUnikedEventPayload payload = event.getPayload();
        articleLikeCountRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getArticleLikeCount(),
                TimeCalculatorUtils.calculateDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<ArticleUnikedEventPayload> event) {
        return EventType.ARTICLE_UNLIKED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleUnikedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
