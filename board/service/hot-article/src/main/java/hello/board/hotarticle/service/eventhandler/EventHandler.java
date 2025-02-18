package hello.board.hotarticle.service.eventhandler;

import hello.board.common.event.Event;
import hello.board.common.event.EventPayload;

/**
 * 이벤트 처리
 * 제너릭으로 eventPayload 를 받는다.
 * */
public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);    //이벤트를 받았을 때 처리되는 로직
    boolean supports(Event<T> event);   // 이벤트 핸들러 구현체가 이벤트를 지원하는지 확인하는 메소드
    Long findArticleId(Event<T> event); // 이벤트가 어떤 아티클에 대한건지 id를 찾아주는 메소드
}
