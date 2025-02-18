package hello.board.hotarticle.service.eventhandler;

import hello.board.common.event.Event;
import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleDeletedEventPayload;
import hello.board.hotarticle.repository.ArticleCreatedTimeRepository;
import hello.board.hotarticle.repository.HotArticleListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final HotArticleListRepository hotArticleListRepository; // 삭제는 인기글에서도 지워저야한다.
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository; // article 생성시간 데이터에서도 삭제

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();
        articleCreatedTimeRepository.delete(payload.getArticleId());
        hotArticleListRepository.remove(payload.getArticleId(), payload.getCreatedAt());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return EventType.ARTICLE_DELETED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleDeletedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
