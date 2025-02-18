package hello.board.hotarticle.service;

import hello.board.common.event.Event;
import hello.board.hotarticle.repository.ArticleCreatedTimeRepository;
import hello.board.hotarticle.repository.HotArticleListRepository;
import hello.board.hotarticle.service.eventhandler.EventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreUpdaterTest {

    @InjectMocks
    HotArticleScoreUpdater hotArticleScoreUpdater;
    @Mock
    HotArticleListRepository hotArticleListRepository;
    @Mock
    HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock
    ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Test
    void updateIfArticleNoCreatedTodayTest() {  // 인기글이 오늘 생성되지 않았으면
        // given
        Long articleId = 1L;
        Event event = mock(Event.class); // mock 메소드에 클래스 타입을 주면 클래스 타입에 대해 가짜 객체 생성
        EventHandler eventHandler = mock(EventHandler.class);

        given(eventHandler.findArticleId(event)).willReturn(articleId);

        LocalDateTime createdTime = LocalDateTime.now().minusDays(1);
        given(articleCreatedTimeRepository.read(articleId)).willReturn(createdTime);

        // when
        hotArticleScoreUpdater.update(event, eventHandler);

        //then
        verify(eventHandler, never()).handle(event);  //event.handler 는 호출되지 않는다.
        verify(hotArticleListRepository, never())
                .add(anyLong(), any(LocalDateTime.class), anyLong(), anyLong(), any(Duration.class));
    }

    @Test
    void updateTest() {  // 인기글이 오늘 생성 됬으면
        // given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        given(eventHandler.findArticleId(event)).willReturn(articleId);

        LocalDateTime createdTime = LocalDateTime.now();
        given(articleCreatedTimeRepository.read(articleId)).willReturn(createdTime);

        // when
        hotArticleScoreUpdater.update(event, eventHandler);

        //then
        verify(eventHandler).handle(event);  //event.handler 는 호출되지 않는다.
        verify(hotArticleListRepository).add(anyLong(), any(LocalDateTime.class), anyLong(), anyLong(), any(Duration.class));
    }
}