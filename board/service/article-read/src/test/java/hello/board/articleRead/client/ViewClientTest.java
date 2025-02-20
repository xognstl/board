package hello.board.articleRead.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}