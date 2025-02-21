package hello.board.articleRead.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OptimizedCacheable {
    String type();  // 어떤 메소드에 붙일지 유니크하게 구분하기 위한 타입
    long ttlSeconds();
}
