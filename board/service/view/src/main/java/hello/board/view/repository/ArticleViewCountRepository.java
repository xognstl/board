package hello.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {
    private final StringRedisTemplate redisTemplate;

    // view::aritcle::{article_id}::view_count
    private static final String KEY_FORMAT = "view::aritcle::%s::view_count";

    // 조회수를 읽는 메소드
    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));// 키 생성해서 조회
        return result == null ? 0L : Long.valueOf(result);
    }

    public Long increase(Long articleId) {
        return redisTemplate.opsForValue().increment(generateKey(articleId));
    }

    private String generateKey(Long articleId) {    // 키 생성
        return KEY_FORMAT.formatted(articleId);
    }
}
