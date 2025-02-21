package hello.board.articleRead.cache;

import hello.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 캐시를 가져와서 처리, 원본데이터 요청
 * */
@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate; // 캐시 서버
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    // 캐시 키로 파라미터가 여러가지 전달될 수 있는거에대한 구분자
    private static final String DELIMITER = "::";

    // 캐시 or 원본데이터 에대해서 처리된 결과
    // 캐시 타입, ttl시간, 캐시에 대해 유니크하게 구분하기 위한 타입별 파라미터, object에 대한 returnType,
    // 캐시가 만료됬을때 원본데이터를 가져오기위한 메소드
    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {
        String key = generateKey(type, args);

        String cachedData = redisTemplate.opsForValue().get(key);
        if (cachedData == null) {   // 캐시에 데이터가 없을때
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);// 캐시 데이터가 있는 상황
        if (optimizedCache == null) {   // 역직렬화가가 잘안됬으면 다시
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        if (!optimizedCache.isExpired()) {  // 논리적으로 만료되지 않았으면 데이터 그대로 반환
            return optimizedCache.parseData(returnType);
        }

        if (!optimizedCacheLockProvider.lock(key)) {    // 논리적으로 만료가 되니 갱신 , 갱신할때 한건의 요청만 갈수 있도록 락
            return optimizedCache.parseData(returnType);
        }
        try { // 락을 획득한 한건의 요청만 온다
            return refresh(originDataSupplier, key, ttlSeconds);
        }finally {
            optimizedCacheLockProvider.unlock(key); // 락 해제
        }
    }

    //원본데이터에 요청 하고 Cache에 데이터를 적재한다.
    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        Object result = originDataSupplier.get(); // 원본 데이터
        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);
        OptimizedCache optimizedCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTTL());// 원본데이터 캐시에 적재

        redisTemplate.opsForValue()
                .set(
                        key,
                        DataSerializer.serialize(optimizedCache),
                        optimizedCacheTTL.getPhysicalTTL()
                );
        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(Collectors.joining(DELIMITER));
        // prefix = a, args = [1,2] => a::1::2
    }
}
