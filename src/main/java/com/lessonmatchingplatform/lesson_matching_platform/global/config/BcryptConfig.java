package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class BcryptConfig {

    @Bean(name = "bcryptExecutor")
    @Profile("!perf")
    public Executor bcryptExecutor() {
        return new ThreadPoolExecutor(
                10,                                          // corePoolSize
                20,                                          // maxPoolSize
                60L, TimeUnit.SECONDS,                       // keepAliveTime
                new ArrayBlockingQueue<>(50),                // 대기 큐 (50개 초과 시 거절)
                r -> {
                    Thread t = new Thread(r);
                    t.setName("bcrypt-" + t.threadId());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()         // 큐 초과 시 RejectedExecutionException
        );
    }

    @Bean(name = "bcryptExecutor")
    @Profile("perf")
    public Executor perfBcryptExecutor() {
        return new SyncTaskExecutor();
    }
}
