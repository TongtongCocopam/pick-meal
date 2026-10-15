package kongju.pickmeal.infrastructure.monitoring;

import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Component;


/**
 * AI 식단 생성 작업의 메트릭을 모니터링하는 클래스
 */
@Component
public class DietGenerationMetrics {
    private final Counter completed;
    private final Counter failed;

    private final AtomicInteger inProgress = new AtomicInteger(0);

    /**
     * 마이크로미터 메트릭 생성
     * @param registry 메트릭 레지스트리
     */
    public DietGenerationMetrics(MeterRegistry registry) {
        this.completed = Counter.builder("diet.generation.completed")
                .description("완료된 AI 식단 생성 작업 수")
                .register(registry);
        this.failed = Counter.builder("diet.generation.failed")
                .description("실패한 AI 식단 생성 작업 수")
                .register(registry);

        Gauge.builder("diet.generation.in_progress", inProgress, AtomicInteger::get)
                .description("진행 중인 AI 식단 생성 작업 수")
                .register(registry);
    }

    public void start(){
        inProgress.incrementAndGet();
    }

    public void complete(){
        completed.increment();
    }

    public void fail(){
        failed.increment();
    }

    public void finish(){
        inProgress.decrementAndGet();
    }

}
