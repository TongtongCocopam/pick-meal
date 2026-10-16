package kongju.pickmeal.infrastructure.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "dietTaskExecutor")
    public Executor dietTaskExecutor(MeterRegistry registry) {
        Counter rejectedCounter = Counter.builder("diet.generation.executor.rejected")
                .description("식단 생성 Executor에서 거절된 작업 수")
                .register(registry);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("diet-async-");

        // 풀이 차면 ThreadPoolExecutor가 RejectedExecutionHandler를 호출함
        ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
        executor.setRejectedExecutionHandler((task, executor1) -> {
            rejectedCounter.increment();

            // 기존 기본 동작처럼 작업 거절
            abortPolicy.rejectedExecution(task, executor1);
        });


        executor.initialize();
        return executor;
    }
}
