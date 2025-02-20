package hello.board.articleRead.service.event.handler;

import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
