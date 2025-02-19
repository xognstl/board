package hello.board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator; // 살아있는 app 떠있는지 확인, 자기한테 할당된 샤드가 몇개인지 반환
    private final KafkaTemplate<String, String> messageRelayTemplate; // config 에서 정의한 messageRelayTemplate() 주입

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)   // 트랜잭션에 대한 이벤트를 받을 수 있다.
    public void createOutbox(OutboxEvent outboxEvent) { // commit 전에 outboxEvent를 받아서 repository 에 저장
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        // commit 되기 전이니 데이터에 대한 비즈니스 로직이 처리되고 트랜잭션에 단일하게 묶였다.
        outboxRepository.save(outboxEvent.getOutbox());
    }

    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)  // 트랜잭션이 commit 되면 비동기로 카프카 이벤트 전송
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());//실제 발행을 시행, OutboxEvent에서 outbox만 꺼내 kafka 로 전송
    }

    private void publishEvent(Outbox outbox) {
        try {
                // kafka에 전송하는 키, shard 키면 동일한 kafka 파티션으로 전송, 동일한 카프카 파티션으로 전송되면 순서 보장
                // outbox가 동일한 shardkey에 대해서는 카프카에서 동일한 순서로 전송된다.
            messageRelayTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS) ; //get 하면 결과를 기달릴 수있따.
            outboxRepository.delete(outbox);
        } catch (Exception e) {
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
    }

    // 10초 동안 전송 안된 이벤트를 주기적으로 polling
    @Scheduled(
            fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvent() {
        AssignedShard assignedShard = messageRelayCoordinator.assignedShards(); // 자기한테 할당된 샤드 목록
        log.info("[MessageRelay.publishPendingEvent] assignedShard size={}", assignedShard.getShards().size());
        for (Long shard : assignedShard.getShards()) {
            List<Outbox> outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10),
                    Pageable.ofSize(100)
            );
            for (Outbox outbox : outboxes) {
                publishEvent(outbox);   // 전송
            }
        }
    }
}
