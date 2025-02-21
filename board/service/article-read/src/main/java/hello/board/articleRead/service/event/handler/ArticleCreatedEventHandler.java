package hello.board.articleRead.service.event.handler;

import hello.board.articleRead.repository.ArticleIdListRepository;
import hello.board.articleRead.repository.ArticleQueryModel;
import hello.board.articleRead.repository.ArticleQueryModelRepository;
import hello.board.articleRead.repository.BoardArticleCountRepository;
import hello.board.common.event.Event;
import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;  // 게시글 목록 저장
    private final BoardArticleCountRepository boardArticleCountRepository;  // 게시글 수 저장
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }
}
