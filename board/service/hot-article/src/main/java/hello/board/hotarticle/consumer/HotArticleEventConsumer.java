package hello.board.hotarticle.consumer;

import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;
import hello.board.common.event.EventType;
import hello.board.hotarticle.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {
    private final HotArticleService hotArticleService;

    @KafkaListener(topics = {
            EventType.Topic.BOARD_ARTICLE,
            EventType.Topic.BOARD_COMMENT,
            EventType.Topic.BOARD_LIKE,
            EventType.Topic.BOARD_VIEW
    })  // topic을 구독해서 이벤트 처리
    public void listen(String message, Acknowledgment ack) {    //Acknowledgment 로 잘처리됬으면 kafka에 알려줄수있다.
        log.info("[HotArticleEventConsumer.listen] received message={}", message);
        Event<EventPayload> event = Event.fromJson(message); // json -> 이벤트 객체로 전환
        if (event != null) {
            hotArticleService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
