package hello.board.articleRead.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Redis에 게시글 Id만 따로 sortedSet으로 저장
 * */
@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    public void add(Long boardId, Long articleId, long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            // score 가 double값이라 toPaddedString 로 변경(articleId는 Long이라 데이터가 꼬일수 있따)
            // score가 고정값이면 value로 정렬상태가 만들어진다. 최신순정렬상태로 유지가능
            conn.zAdd(key, 0, toPaddedString(articleId));
            conn.zRemRange(key, 0, -limit - 1);    // 상위 1000개 유지
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        //reverseRange 정렬된 상태로 조회
        return redisTemplate.opsForZSet()
                .reverseRange(generateKey(boardId), offset, offset + limit - 1)
                .stream().map(Long::valueOf).toList();
    }

    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        // 스코어가 0으로 동일한거에대해 value 값으로 정렬이 되는데 정렬된 상태를 조회하려면 파라미터로 문자를 줘야한다. reverseRangeByLex 사용
        return redisTemplate.opsForZSet().reverseRangeByLex(
                generateKey(boardId),
                // 문자열상태로 데이터가 정렬된 상태로 들어온다. 6,5,4,3,2,1 일때 lastArticleId == null 일때 limit 개수만 가져온다.
                // ex) limit 3 => 6,5,4 이렇게되면 lastArticle = 4 , exclusive 4는 제외 => 3,2,1 이나온다.
                lastArticleId == null ?
                        Range.unbounded() : // 1페이지
                        Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastArticleId))),
                Limit.limit().count(limit.intValue())
        ).stream().map(Long::valueOf).toList();
    }



    // long 값으로 받은 파라미터를 고정된 자릿수의 문자열로 바꿔준다.
    private String toPaddedString(Long articleId) {
        //1234 -> 0000000000000001234
        return "%019d".formatted(articleId);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}
