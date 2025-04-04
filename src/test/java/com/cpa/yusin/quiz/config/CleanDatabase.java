package com.cpa.yusin.quiz.config;

import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

public abstract class CleanDatabase
{
    public static void teardown(ApplicationContext context)
    {
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
