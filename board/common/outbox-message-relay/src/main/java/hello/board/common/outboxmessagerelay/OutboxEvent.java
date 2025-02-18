package hello.board.common.outboxmessagerelay;

import lombok.Getter;
import lombok.ToString;

/**
 * Outbox Event 전달
 * */
@Getter
@ToString
public class OutboxEvent {
    private Outbox outbox;

    public static OutboxEvent of(Outbox outbox) {
        OutboxEvent event = new OutboxEvent();
        event.outbox = outbox;
        return event;
    }
}
