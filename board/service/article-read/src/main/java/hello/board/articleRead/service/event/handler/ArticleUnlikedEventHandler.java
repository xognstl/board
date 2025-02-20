package hello.board.articleRead.service.event.handler;

import hello.board.articleRead.repository.ArticleQueryModelRepository;
import hello.board.common.event.Event;
import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleLikedEventPayload;
import hello.board.common.event.payload.ArticleUnikedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUnlikedEventHandler implements EventHandler<ArticleUnikedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;
    @Override
    public void handle(Event<ArticleUnikedEventPayload> event) {
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleUnikedEventPayload> event) {
        return EventType.ARTICLE_UNLIKED == event.getType();
    }
}
