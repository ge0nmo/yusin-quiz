package com.cpa.yusin.quiz.config;

import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public abstract class CleanDatabase
{
    public static void teardown(ApplicationContext context)
    {
        awaitAsyncTasks(context);
        EntityManager entityManager = context.getBean(EntityManager.class);
        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        TransactionTemplate transactionTemplate = context.getBean(TransactionTemplate.class);

        transactionTemplate
                .execute(status -> {
                    entityManager.clear();
                    deleteAll(jdbcTemplate);
                    return null;
                });
    }

    /** 이전 테스트의 AFTER_COMMIT 비동기 쓰기가 TRUNCATE 뒤에 도착해 다음 테스트를 오염시키지 않게 한다. */
    private static void awaitAsyncTasks(ApplicationContext context)
    {
        if (!context.containsBean("taskExecutor")) {
            return;
        }
        Object executorBean = context.getBean("taskExecutor");
        if (!(executorBean instanceof ThreadPoolTaskExecutor executor)) {
            return;
        }

        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (executor.getActiveCount() > 0 || !executor.getThreadPoolExecutor().getQueue().isEmpty()) {
            if (System.nanoTime() >= deadline) {
                throw new IllegalStateException("Timed out waiting for asynchronous test tasks");
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("Interrupted while waiting for asynchronous test tasks");
            }
            LockSupport.parkNanos(Duration.ofMillis(10).toNanos());
        }
    }

    public static void deleteAll(JdbcTemplate jdbcTemplate)
    {
        List<String> tables = findTables(jdbcTemplate);
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        for(String table : tables)
        {
            jdbcTemplate.execute(String.format("TRUNCATE TABLE %s", table));
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
    }

    public static List<String> findTables(JdbcTemplate jdbcTemplate)
    {
        return jdbcTemplate.query("SHOW TABLES", (rs, rowNum) -> rs.getString(1))
                .stream()
                .toList();
    }
}
