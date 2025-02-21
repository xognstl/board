package hello.board.articleRead.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// ViewService 실행 후 테스트
@SpringBootTest
class ViewClientTest {
    @Autowired
    ViewClient viewClient;

    @Test
    void readCacheableTest() throws InterruptedException {
        // 처음엔 캐시에 데이터가 없어서 로그 출력 이후에는 캐시에 데이터가 있기때문에 미출력
        viewClient.count(1L);   // 로그 출력
        viewClient.count(1L);   // 로그 미출력
        viewClient.count(1L);

        TimeUnit.SECONDS.sleep(3);
        viewClient.count(1L);   // 1초로 캐시 만료 설정 해놨기 떄문에 로그 출력
    }

    @Test
    void readCacheableMultiThreadTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);  // 5개 동시요청

        viewClient.count(1L); // 초기화 용도 (캐시가 없기에)

        for (int i = 0; i < 5; i++) {
            CountDownLatch latch = new CountDownLatch(5);// 스레드 풀로 동시에 5개요청
            for (int j = 0; j < 5; j++) {
                executorService.execute(() -> {
                    viewClient.count(1L);
                    latch.countDown();
                });
            }
            latch.await();
            TimeUnit.SECONDS.sleep(2);
            System.out.println("==== cache expired ====");
        }
    }
}