package hello.board.common.outboxmessagerelay;

import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;
import hello.board.common.event.EventType;
import hello.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * outbox 이벤트를 만드는 이벤트 퍼블리셔
 * */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final Snowflake outboxIdSnowflake = new Snowflake();
    private final Snowflake eventIdSnowflake = new Snowflake();
    private final ApplicationEventPublisher applicationEventPublisher;

    // article Service 에서 이 모듈을 가져와서 OutboxEventPublisher를 통해 이벤트 발행
    public void publish(EventType type, EventPayload payload, Long shardKey) {
        // articleId = 10 , shardKey == articleId
        // 10 % 4 = 물리적샤드 2
        Outbox outbox = Outbox.create(
                outboxIdSnowflake.nextId(),
                type,
                Event.of(
                        eventIdSnowflake.nextId(), type, payload
                ).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox)); // 메시지 발행 -> messageRelay에서 수신해서 처리
    }
}
