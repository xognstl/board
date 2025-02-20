package hello.board.articleRead.service;

import hello.board.articleRead.client.ArticleClient;
import hello.board.articleRead.client.CommentClient;
import hello.board.articleRead.client.LikeClient;
import hello.board.articleRead.client.ViewClient;
import hello.board.articleRead.repository.ArticleQueryModel;
import hello.board.articleRead.repository.ArticleQueryModelRepository;
import hello.board.articleRead.service.event.handler.EventHandler;
import hello.board.articleRead.service.response.ArticleReadResponse;
import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {
    // 데이터 없을때 command 서버로 요청 해야하니 필요 client 주입
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final List<EventHandler> eventHandlers;

    public void handleEvent(Event<EventPayload> event) {    // consumer 를 통해 호출
        for (EventHandler eventHandler : eventHandlers) {
            if(eventHandler.supports(event)) {
                eventHandler.handle(event);
            }
        }
    }

    // 게시글 데이터 조회 메소드
    public ArticleReadResponse read(Long articleId) {
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId)
                .or(() -> fetch(articleId))   // data 가 없으면? fetch 에서 command Service 에서 데이터 호출
                .orElseThrow();

        return ArticleReadResponse.from(articleQueryModel, viewClient.count(articleId));
    }

    private Optional<ArticleQueryModel> fetch(Long articleId) {
        //articleClient 호출한거 queryModel 생성 -> repository 저장
        Optional<ArticleQueryModel> articleQueryModelOptional = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.create(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId)
                ));
        articleQueryModelOptional.ifPresent(articleQueryModel -> {
            articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1));
        });
        log.info("[ArticleReadService.fetch] fetch data, articleId={}, isPresent={}", articleId, articleQueryModelOptional.isPresent());
        return articleQueryModelOptional;
    }
}
