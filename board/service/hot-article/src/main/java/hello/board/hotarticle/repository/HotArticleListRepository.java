package hello.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 인기글을 Redis에 저장
 * */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // SortedSet 으로 하나의 키에 여러개의 게시글 아이디와 스코어를 저장
    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article-list:%s";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        // executePipelined : redis쪽으로 네트워크 통신 한번만 연결하면 여러개의 연산을 한번에 수행
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(time); // 키생성
            conn.zAdd(key, score, String.valueOf(articleId));   // sortedSet 사용
            conn.zRemRange(key, 0, - limit - 1); // 상위 limit 갯수만큼 sortedSet 데이터를 유지할 수 있다.
            conn.expire(key, ttl.toSeconds());
            return null;
        });
    }

    // 게시글 삭제시 인기글에서도 사라져야하는것 처리
    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    public List<Long> readAll(String dateStr) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(generateKey(dateStr), 0, -1).stream()
                .peek(tuple ->
                        log.info("[HotArticleListRepository.readAll] articleId={}, score={}", tuple.getValue(), tuple.getScore()))
                .map(ZSetOperations.TypedTuple::getValue)// TypedTuple<String>에서 articleId 만 가져옴
                .map(Long::valueOf) // String -> Long
                .toList();
    }
}
