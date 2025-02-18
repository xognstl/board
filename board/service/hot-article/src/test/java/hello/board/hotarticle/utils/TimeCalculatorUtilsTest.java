package hello.board.hotarticle.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TimeCalculatorUtilsTest {
    @Test
    void test() {
        Duration duration = TimeCalculatorUtils.calculateDurationToMidnight();
        System.out.println("duration = " + duration.getSeconds() / 60);
        // 09:17 기준 883 이 나옴 => 자정까지 883분 남음
    }
}
