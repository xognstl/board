package hello.board.common.event;

import hello.board.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.BOARD_ARTICLE),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnikedEventPayload.class, Topic.BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.BOARD_VIEW);

    // 이이벤트들이 어떤 payload 타입인지 그 타입을 가질수 있다.
    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    // 이벤트 타입 문자열로 받아서 enum 타입으로 변환해주는 메소드
    public static EventType from(String type) {
        try {
            return valueOf(type);
        }catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    // kafka topic
    public static class Topic {
        public static final String BOARD_ARTICLE = "board-article";
        public static final String BOARD_COMMENT = "board-comment";
        public static final String BOARD_LIKE = "board-like";
        public static final String BOARD_VIEW = "board-view";
    }
}
