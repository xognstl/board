package hello.board.articleRead.cache;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OptimizedCacheAspect {
    private final OptimizedCacheManager optimizedCacheManager;

    @Around("@annotation(OptimizedCacheable)")   // 메소드 전후로 실행
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OptimizedCacheable cacheable = findAnnotation(joinPoint);
        return optimizedCacheManager.process(
                cacheable.type(),
                cacheable.ttlSeconds(),
                joinPoint.getArgs(), // 메소드에 선언된 파라미터
                findReturnType(joinPoint),  // 리턴 타입
                () -> joinPoint.proceed() // 원본데이터
        );
    }

    // ProceedingJoinPoint 이 수행되는 메소드에서 OptimizedCacheable 어노테이션이 달려있는지 찾아올수 있따.
    private OptimizedCacheable findAnnotation(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        // findAnnotation 메소드에 OptimizedCacheable 애노테이션이 달려있는지 꺼내올 수 있다.
        return methodSignature.getMethod().getAnnotation(OptimizedCacheable.class);
    }

    private Class<?> findReturnType(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }
}
