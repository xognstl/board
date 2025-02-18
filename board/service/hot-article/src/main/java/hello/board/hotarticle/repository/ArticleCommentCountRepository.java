package hello.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 데이터들을 그 인기글 서비스의 책임으로 보고
 * 자체적으로 인기글이 만들어지는동안 여러개의 데이터들을 가지고 있는다.
 * 가지고 있지 않으면 댓글서비스로 조회하기 위해서 계속 호출이 필요
 * 댓글 수 저장
 * */
@Repository
@RequiredArgsConstructor
public class ArticleCommentCountRepository {
    private final StringRedisTemplate redisTemplate;

    //hot-article::article::{articleId}::comment-count
    private static final String KEY_FORMAT = "hot-article::article::%s::comment-count";

    public void createOrUpdate(Long articleId, Long commentCount, Duration ttl) {
        //인기글이 선정될때까지만 가지고 있으면 되니까 시간만료
        redisTemplate.opsForValue().set(generateKey(articleId), String.valueOf(commentCount), ttl);// set 데이터가 있으면 update 없으면 create
    }

    //조회
    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0L : Long.valueOf(result);
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
