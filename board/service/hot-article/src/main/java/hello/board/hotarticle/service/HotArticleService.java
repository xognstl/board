package hello.board.hotarticle.service;

import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;
import hello.board.common.event.EventType;
import hello.board.hotarticle.client.ArticleClient;
import hello.board.hotarticle.repository.HotArticleListRepository;
import hello.board.hotarticle.service.eventhandler.EventHandler;
import hello.board.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {
    private final ArticleClient articleClient;  // 인기글 조회시 articleClient를 통해 원본 게시글 정보 조회, 같이 응답
    private final List<EventHandler> eventHandlers; // 리스트로 주입하면 구현체들이 다같이 주입
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;    // 인기글 조회시 사용

    // 이벤트를 통해서 인기글 점수를 계산후 HotArticleListRepository에 인기글 id를 저장하는 메소드
    public void handleEvent(Event<EventPayload> event) {    // handle event 인기글 서비스는 Consumer 기 때문에 kafka를 통해서 전달 받는다.
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if(eventHandler == null) {
            return;
        }

        if (isArticleCreatedOrDeleted(event)) { // 이벤트가 게시글 생성 or 삭제 이벤트인지 검사
            eventHandler.handle(event);
        } else {
            hotArticleScoreUpdater.update(event, eventHandler);
        }

    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {    // 이벤트 지원 여부 확인
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny().orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }

    // 조회하는 메소드
    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr).stream()
                .map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }
}
