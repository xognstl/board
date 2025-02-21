package hello.board.articleRead.service;

import hello.board.articleRead.client.ArticleClient;
import hello.board.articleRead.client.CommentClient;
import hello.board.articleRead.client.LikeClient;
import hello.board.articleRead.client.ViewClient;
import hello.board.articleRead.repository.ArticleIdListRepository;
import hello.board.articleRead.repository.ArticleQueryModel;
import hello.board.articleRead.repository.ArticleQueryModelRepository;
import hello.board.articleRead.repository.BoardArticleCountRepository;
import hello.board.articleRead.service.event.handler.EventHandler;
import hello.board.articleRead.service.response.ArticleReadPageResponse;
import hello.board.articleRead.service.response.ArticleReadResponse;
import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final ArticleIdListRepository articleIdListRepository;  // 게시글 목록 저장
    private final BoardArticleCountRepository boardArticleCountRepository;  // 게시글 수 저장

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

    // 페이지 번호 방식
    public ArticleReadPageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticleReadPageResponse.of(
                readAll(
                        readAllArticleIds(boardId, page, pageSize)
                ),
                count(boardId)
        );
    }

    // articleids 를 받아서 ArticleReadResponse 로 변환해서 반환
    private List<ArticleReadResponse> readAll(List<Long> articleIds) {
        Map<Long, ArticleQueryModel> articleQueryModelMap = articleQueryModelRepository.readAll(articleIds);
        return articleIds.stream()
                .map(articleId -> articleQueryModelMap.containsKey(articleId) ?
                        articleQueryModelMap.get(articleId) :
                        fetch(articleId).orElse(null))  //원본데이터
                .filter(Objects::nonNull)
                .map(articleQueryModel ->
                        ArticleReadResponse.from(
                                articleQueryModel,
                                viewClient.count(articleQueryModel.getArticleId())
                        ))
                .toList();
    }

    private List<Long> readAllArticleIds(Long boardId, Long page, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAll(boardId, (page - 1) * pageSize, pageSize);
        if(pageSize == articleIds.size()) {   // 게시글 목록이 전부가 레디스에 저장되있다.
            log.info("[ArticleReadService.readAllArticleIds] return redis data");
            return articleIds;
        }
        log.info("[ArticleReadService.readAllArticleIds] return origin data");
        // 원본데이터
        return articleClient.readAll(boardId, page, pageSize).getArticles().stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }


    private long count(Long boardId) {
        Long result = boardArticleCountRepository.read(boardId);
        if(result != null) {
            return result;
        }
        long count = articleClient.count(boardId);
        boardArticleCountRepository.createOrUpdate(boardId, count);
        return count;
    }

    public List<ArticleReadResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        return readAll(
                readAllInfiniteScrollArticleIds(boardId, lastArticleId, pageSize)
        );
    }

    private List<Long> readAllInfiniteScrollArticleIds(Long boardId, Long lastArticleId, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
        if(pageSize == articleIds.size()) {
            log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return redis data");
            return articleIds;
        }
        log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return origin data");
        return articleClient.readAllInfiniteScroll(boardId, lastArticleId, pageSize).stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }
}
