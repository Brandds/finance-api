package com.brandon.finance.shared.base.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@JsonTest
@ContextConfiguration(classes = PageSerializationTest.JsonConfig.class)
class PageSerializationTest {

    @Configuration
    @Import(SpringDataJacksonConfiguration.class)
    @ComponentScan(basePackages = "com.brandon.finance.shared.base.response", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = JsonComponent.class))
    static class JsonConfig {}

    @Autowired ObjectMapper objectMapper;

    private final Logger pageLogger = (Logger) LoggerFactory.getLogger(
        "org.springframework.data.web.config.SpringDataJacksonConfiguration$PageModule$WarningLoggingModifier");
    private final ListAppender<ILoggingEvent> pageLogs = new ListAppender<>();

    @BeforeEach
    void capturePageSerializationWarnings() {
        pageLogs.start();
        pageLogger.addAppender(pageLogs);
    }

    @Test
    void preservesPagedResponseContractWithoutSerializingPageImplDirectly() throws Exception {
        var page = new PageImpl<>(List.of("Alimentação", "Transporte"),
            PageRequest.of(1, 2, Sort.by("name")), 5);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new ApiResponse<>(page, "Categorias")));

        assertThat(json).isEqualTo(objectMapper.readTree("""
            {
              "data": {
                "content": ["Alimentação", "Transporte"],
                "pageable": {
                  "pageNumber": 1, "pageSize": 2, "offset": 2,
                  "paged": true, "unpaged": false,
                  "sort": {"empty": false, "sorted": true, "unsorted": false}
                },
                "totalElements": 5, "totalPages": 3, "size": 2, "number": 1,
                "first": false, "last": false, "numberOfElements": 2, "empty": false,
                "sort": {"empty": false, "sorted": true, "unsorted": false}
              },
              "message": "Categorias"
            }
            """));
    }

    @Test
    void preservesEmptyPageMetadata() throws Exception {
        var page = Page.empty(PageRequest.of(0, 20));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(page));

        assertThat(json.get("content").isEmpty()).isTrue();
        assertThat(json.get("totalElements").asLong()).isZero();
        assertThat(json.get("totalPages").asInt()).isZero();
        assertThat(json.get("size").asInt()).isEqualTo(20);
        assertThat(json.get("number").asInt()).isZero();
        assertThat(json.get("first").asBoolean()).isTrue();
        assertThat(json.get("last").asBoolean()).isTrue();
        assertThat(json.get("empty").asBoolean()).isTrue();
        assertThat(json.at("/sort/unsorted").asBoolean()).isTrue();
    }

    @Test
    void preservesUnpagedRepresentationWithoutCallingUnsupportedPageableMethods() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new PageImpl<>(List.of("Categoria"))));

        assertThat(json.get("pageable").asText()).isEqualTo("INSTANCE");
        assertThat(json.get("totalElements").asLong()).isEqualTo(1);
        assertThat(json.get("size").asInt()).isEqualTo(1);
        assertThat(json.get("totalPages").asInt()).isEqualTo(1);
        assertThat(json.get("first").asBoolean()).isTrue();
        assertThat(json.get("last").asBoolean()).isTrue();
    }

    @AfterEach
    void doesNotEmitDirectPageSerializationWarning() {
        pageLogger.detachAppender(pageLogs);
        pageLogs.stop();
        assertThat(pageLogs.list).isEmpty();
    }
}
