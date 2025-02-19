package hello.board.common.outboxmessagerelay;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync    // 트랜잭션이 끝나면 kafka에 대한 이벤트 전송을 비동기로 처리
@Configuration
@ComponentScan("hello.board.common.outboxmessagerelay")
@EnableScheduling   // 전송되지 않은 이벤트를 주기적으로 가져와서 polling 해 kafka로  보낼것
public class MessageRelayConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean   // kafka 로 프로듀서 애플리케이션들이 이벤트를 전송
    public KafkaTemplate<String, String> messageRelayKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();  // kafka producer 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // hot-article yml 을 보면 StringDeserializer
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }


    // 트랜잭션이 끝날 떄마다 이벤트 전송을 비동기로 전송하기 위해 처리하는 스레드 풀
    @Bean
    public Executor messageRelayPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mr-pub-event-");
        return executor;
    }

    // 이벤트 전송이 아직 되지 않은 것들 10초 이후에 이벤트를 주기적으로 보내주기 위한 스레드풀
    @Bean
    public Executor messageRelayPublishPendingEventExecutor() {
        // 각 애플리케이션 마다 shard 가 분할 되어 할당 되기 떄문에 싱글스레드로 미전송 이벤트 전송
        return Executors.newSingleThreadScheduledExecutor();
    }
}
