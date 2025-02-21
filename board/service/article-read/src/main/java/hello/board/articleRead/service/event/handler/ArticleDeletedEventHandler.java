package hello.board.articleRead.service.event.handler;

import hello.board.articleRead.repository.ArticleIdListRepository;
import hello.board.articleRead.repository.ArticleQueryModelRepository;
import hello.board.articleRead.repository.BoardArticleCountRepository;
import hello.board.common.event.Event;
import hello.board.common.event.EventType;
import hello.board.common.event.payload.ArticleDeletedEventPayload;
import hello.board.common.event.payload.ArticleUpdatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;  // 게시글 목록 저장
    private final BoardArticleCountRepository boardArticleCountRepository;  // 게시글 수 저장
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();
        articleIdListRepository.delete(payload.getBoardId(), payload.getArticleId());
        articleQueryModelRepository.delete(payload.getArticleId());
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
        // 게시글 목록 삭제 -> QueryModel 삭제 -> Count update
        //query model 삭제되면 게시글은 삭제되었지만 목록에는 존재해서 사이에 조회를 하면 에러가 날수있다.
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return EventType.ARTICLE_DELETED == event.getType();
    }
}
