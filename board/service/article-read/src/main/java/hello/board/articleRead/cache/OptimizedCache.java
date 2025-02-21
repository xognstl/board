package hello.board.articleRead.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hello.board.common.dataserializer.DataSerializer;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * OptimizedCache 데이터를 Redis에 저장해준다.
 * */
@Getter
@ToString
public class OptimizedCache {
    private String data;
    private LocalDateTime expiredAt;   // Logical TTL

    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache optimizedCache = new OptimizedCache();
        optimizedCache.data = DataSerializer.serialize(data);
        optimizedCache.expiredAt = LocalDateTime.now().plus(ttl);
        return optimizedCache;
    }

    // logical TTL 만료 확인
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    // data -> 실제 객체 타입 변경
    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}
