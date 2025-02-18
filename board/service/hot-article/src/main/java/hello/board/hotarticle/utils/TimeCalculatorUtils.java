package hello.board.hotarticle.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeCalculatorUtils {

    // TTL이 된다./인기글 선정시 당일에만 인기글을 선정하면 되니 TTL 을 걸어 오늘이 지나면 데이터가 더이상 필요 X
    public static Duration calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);
        return Duration.between(now, midnight); // 현재시간에서 자정 까지 얼마가 남았는지
    }
}
