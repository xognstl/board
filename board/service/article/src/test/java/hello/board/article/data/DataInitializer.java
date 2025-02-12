package hello.board.article.data;

import hello.board.article.entity.Article;
import hello.board.common.snowflake.Snowflake;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class DataInitializer {

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT); // CountDownLatch 을 사용하여 6천번 수행을 기다림

    static final int BULK_INSERT_SIZE = 2000; // bulk 로 한번에 2천건
    static final int EXECUTE_COUNT = 6000;  // 6천번 실행

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 멀티 스레드
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            executorService.submit(() -> {
                insert();
                latch.countDown();  //카운트 다운
                System.out.println("latch.getCount() = " + latch.getCount());
                // log 가 많이 남으므로 show sql : false 로 잠시 변경
            });  // 멀티 스레드 실행
        }
        latch.await();  //카운트가 0이될때 까지 기다린다.
        executorService.shutdown(); // 모든 삽입이 끝나면 shutdown
    }

    // 트랜잭션 템플릿을 통해서 2000개의 쿼리를 수행하면 트랜잭션이 종료될 때 한번에 삽입이 수행
    void insert() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < BULK_INSERT_SIZE; i++) {
                Article article = Article.create(
                        snowflake.nextId(),
                        "title" + i,
                        "content" + i,
                        1L,
                        1L
                );
                entityManager.persist(article);
            }
        });
    }
}
