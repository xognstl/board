package hello.board.articleRead.cache;

import lombok.Getter;

import java.time.Duration;

/**
 * TTL 을 받아서 Logical TTL , Physical TTL 계산
 * */
@Getter
public class OptimizedCacheTTL {
    private Duration logicalTTL;
    private Duration physicalTTL;

    public static final long PHYSICAL_TTL_DELAY_SECONDS = 5;

    // PhysicalTTL 은 Logical TTL 보다 + 5초 커야한다.
    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTTL = Duration.ofSeconds(ttlSeconds);
        optimizedCacheTTL.physicalTTL = optimizedCacheTTL.logicalTTL.plusSeconds(PHYSICAL_TTL_DELAY_SECONDS);
        return optimizedCacheTTL;
    }
}
