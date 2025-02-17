package hello.board.common.event;

import hello.board.common.event.payload.ArticleCreatedEventPayload;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class EventTest {
    @Test
    void serde() {  // 직렬화 역직렬화 테스트
        //given
        //payload 생성
        ArticleCreatedEventPayload payload = ArticleCreatedEventPayload.builder()
                .articleId(1L)
                .title("title")
                .content("content")
                .boardId(1L)
                .writerId(1L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .boardArticleCount(25L)
                .build();

        // 이벤트 생성
        Event<EventPayload> event = Event.of(
                1234L,
                EventType.ARTICLE_CREATED,
                payload
        );

        //event -> json
        String json = event.toJson();
        System.out.println("json = " + json);

        //when
        //json -> 객체
        Event<EventPayload> result = Event.fromJson(json);

        //then
        assertThat(result.getEventId()).isEqualTo(event.getEventId());
        assertThat(result.getType()).isEqualTo(event.getType());
        assertThat(result.getPayload()).isInstanceOf(payload.getClass());
        ArticleCreatedEventPayload resultPayload = (ArticleCreatedEventPayload) result.getPayload();

        assertThat(resultPayload.getArticleId()).isEqualTo(payload.getArticleId());
        assertThat(resultPayload.getTitle()).isEqualTo(payload.getTitle());
        assertThat(resultPayload.getCreatedAt()).isEqualTo(payload.getCreatedAt());
    }
}