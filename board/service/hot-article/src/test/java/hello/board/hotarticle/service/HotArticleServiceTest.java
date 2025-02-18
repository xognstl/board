package hello.board.hotarticle.service;

import hello.board.common.event.Event;
import hello.board.common.event.EventType;
import hello.board.hotarticle.service.eventhandler.EventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotArticleServiceTest {
    @InjectMocks
    HotArticleService hotArticleService;
    @Mock
    List<EventHandler> eventHandlers;
    @Mock
    HotArticleScoreUpdater hotArticleScoreUpdater;

    @Test
    void handleEventIfEventHandlerNotFoundTest() {  // 이벤트 핸들러 못찾을때 테스트
        // given
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);
        given(eventHandler.supports(event)).willReturn(false);
        given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));    //findEventHandler

        // when
        hotArticleService.handleEvent(event);

        // then
        verify(eventHandler, never()).handle(event);
        verify(hotArticleScoreUpdater, never()).update(event, eventHandler);
    }

    @Test
    void handleEventIfArticleCreatedEvent() {   // 게시글 생성 이벤트 일때
        // given
        Event event = mock(Event.class);
        given(event.getType()).willReturn(EventType.ARTICLE_CREATED);

        EventHandler eventHandler = mock(EventHandler.class);
        given(eventHandler.supports(event)).willReturn(true);
        given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));    //findEventHandler

        // when
        hotArticleService.handleEvent(event);

        // then
        verify(eventHandler).handle(event);
        verify(hotArticleScoreUpdater, never()).update(event, eventHandler);
    }

    @Test
    void handleEventIfArticleDeletedEvent() {   // 게시글 삭제 이벤트 일때
        // given
        Event event = mock(Event.class);
        given(event.getType()).willReturn(EventType.ARTICLE_DELETED);

        EventHandler eventHandler = mock(EventHandler.class);
        given(eventHandler.supports(event)).willReturn(true);
        given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));    //findEventHandler

        // when
        hotArticleService.handleEvent(event);

        // then
        verify(eventHandler).handle(event);
        verify(hotArticleScoreUpdater, never()).update(event, eventHandler);
    }

    @Test
    void handleEventIfScoreUpdateableEventTest() {   // 스코어 업데이트가 가능한 이벤트
        // given
        Event event = mock(Event.class);
        given(event.getType()).willReturn(mock(EventType.class));

        EventHandler eventHandler = mock(EventHandler.class);
        given(eventHandler.supports(event)).willReturn(true);
        given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));    //findEventHandler

        // when
        hotArticleService.handleEvent(event);

        // then
        verify(eventHandler, never()).handle(event);
        verify(hotArticleScoreUpdater).update(event, eventHandler);
    }
}