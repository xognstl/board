package hello.board.hotarticle.service;

import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;
import hello.board.hotarticle.repository.ArticleCreatedTimeRepository;
import hello.board.hotarticle.repository.HotArticleListRepository;
import hello.board.hotarticle.service.eventhandler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 인기글에 대한 점수를 업데이트 해주는 class
 * */
@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private final HotArticleListRepository hotArticleListRepository;
    private final HotArticleScoreCalculator hotArticleScoreCalculator;
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    private static final long HOT_ARTICLE_COUNT = 10;
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10);

    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {
        Long articleId = eventHandler.findArticleId(event); //event의 Payload를 검사하여 articleId 를 가져옴
        LocalDateTime createdTime = articleCreatedTimeRepository.read(articleId);

        if (!isArticleCreatedToDay(createdTime)) {  // 오늘 생성된게 아니면 처리X
            return;
        }

        eventHandler.handle(event); // 받은 이벤트에 대해 댓글수,좋아요수,조회수 저장을 해줘야한다.

        long score = hotArticleScoreCalculator.calculate(articleId);
        // 업데이트
        hotArticleListRepository.add(
                articleId,
                createdTime,
                score,
                HOT_ARTICLE_COUNT,
                HOT_ARTICLE_TTL
        );
    }

    private boolean isArticleCreatedToDay(LocalDateTime createdTime) {
        return createdTime != null && createdTime.toLocalDate().equals(LocalDate.now());
    }
}
