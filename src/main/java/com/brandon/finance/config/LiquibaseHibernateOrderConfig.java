package com.brandon.finance.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

/**
 * Garante a ordem de execução:
 * 1. DataSource
 * 2. Liquibase (migrations)
 * 3. Hibernate (validação)
 * 
 * Permite usar ddl-auto: validate sem conflitos de timing
 */
@Configuration
@AutoConfigureAfter({
    LiquibaseAutoConfiguration.class,
    DataSourceAutoConfiguration.class
})
@ConditionalOnBean(SpringLiquibase.class)
public class LiquibaseHibernateOrderConfig {
    // Esta classe apenas define a ordem de auto-configuração
    // Liquibase executa antes do Hibernate, evitando problemas de validação
}
