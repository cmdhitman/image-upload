package com.test.imageupload.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Thread poll config for async tasks
 *
 * @author Vladimir Moiseev
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setAwaitTerminationSeconds(5);
        taskExecutor.setCorePoolSize(7);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(50);

        taskExecutor.initialize();

        return taskExecutor;
    }
}
