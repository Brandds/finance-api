package com.brandon.finance.expense.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/** Valida JPQL no Hibernate real sem conectar ao banco; não substitui a execução em PostgreSQL. */
class ExpenseSummaryQueryTest {
    @Test
    void validatesRepositoryQueriesWithPostgresDialectWithoutDatabaseConnection() {
        var dataSource = mock(DataSource.class);
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.brandon.finance");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
            "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
            "hibernate.boot.allow_jdbc_metadata_access", false,
            "hibernate.hbm2ddl.auto", "none"));
        factory.afterPropertiesSet();
        try (var entityManager = factory.getObject().createEntityManager()) {
            // Spring Data valida as consultas @Query ao construir o repositório.
            var repository = new JpaRepositoryFactory(entityManager).getRepository(ExpenseRepository.class);
            assertThat(repository).isNotNull();
            verifyNoInteractions(dataSource);
        } finally {
            factory.destroy();
        }
    }
}
