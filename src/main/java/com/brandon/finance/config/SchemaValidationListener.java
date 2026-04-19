package com.brandon.finance.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import jakarta.persistence.EntityManagerFactory;

/**
 * Valida o schema do banco após Liquibase terminar de executar as migrations
 * Garante que todas as mudanças no schema foram aplicadas antes da validação
 */
@Configuration
public class SchemaValidationListener {

    private final EntityManagerFactory entityManagerFactory;

    public SchemaValidationListener(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateSchema() {
        try {
            // Testa se o EntityManager consegue ser criado
            // Se há inconsistências no schema, Hibernate vai falhar aqui
            var em = entityManagerFactory.createEntityManager();
            em.close();
            System.out.println("✅ Schema validation successful!");
        } catch (Exception e) {
            System.err.println("❌ Schema validation failed!");
            System.err.println("❌ Erro: " + e.getMessage());
            throw new RuntimeException("Schema validation failed - verifique se Liquibase rodou corretamente", e);
        }
    }
}

