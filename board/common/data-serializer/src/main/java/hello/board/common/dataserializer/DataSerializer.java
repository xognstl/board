package hello.board.common.dataserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSerializer {
    private static final ObjectMapper objectMapper = initialize();

    private static ObjectMapper initialize() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())   // 시간관련해서 직렬화 처리하려면 Java time 모듈 등록해야함
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);    // 역직렬화 할때 없는 필드가 있으면 에러가날 수 있는 설정, false는 에러안남
    }

    // String 타입으로 데이터를 받아서 클래스 타입으로 역직렬화 해주는 메서드
    public static <T> T deserialize(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.deserialize] data={}, clazz={}", data, clazz, e);
            return null;
        }
    }

    // Object 로 데이터를 받아서 타입 변환
    public static <T> T deserialize(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    // object 받아서 json 으로 변환
    public static String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.serialize] object={}", object, e);
            return null;
        }
    }
}
