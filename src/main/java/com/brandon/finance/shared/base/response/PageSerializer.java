package com.brandon.finance.shared.base.response;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/** Mantém o contrato de paginação sem depender da serialização interna de PageImpl. */
@JsonComponent
public class PageSerializer extends JsonSerializer<Page<?>> {

    @Override
    public void serialize(Page<?> page, JsonGenerator json, SerializerProvider serializers) throws IOException {
        json.writeStartObject();
        json.writeObjectField("content", page.getContent());
        writePageable(json, page.getPageable());
        json.writeNumberField("totalElements", page.getTotalElements());
        json.writeNumberField("totalPages", page.getTotalPages());
        json.writeNumberField("size", page.getSize());
        json.writeNumberField("number", page.getNumber());
        writeSort(json, page.getSort());
        json.writeBooleanField("first", page.isFirst());
        json.writeBooleanField("last", page.isLast());
        json.writeNumberField("numberOfElements", page.getNumberOfElements());
        json.writeBooleanField("empty", page.isEmpty());
        json.writeEndObject();
    }

    private void writePageable(JsonGenerator json, Pageable pageable) throws IOException {
        if (pageable.isUnpaged()) {
            json.writeStringField("pageable", "INSTANCE");
            return;
        }

        json.writeObjectFieldStart("pageable");
        json.writeNumberField("pageNumber", pageable.getPageNumber());
        json.writeNumberField("pageSize", pageable.getPageSize());
        json.writeNumberField("offset", pageable.getOffset());
        writeSort(json, pageable.getSort());
        json.writeBooleanField("paged", pageable.isPaged());
        json.writeBooleanField("unpaged", pageable.isUnpaged());
        json.writeEndObject();
    }

    private void writeSort(JsonGenerator json, Sort sort) throws IOException {
        json.writeObjectFieldStart("sort");
        json.writeBooleanField("empty", sort.isEmpty());
        json.writeBooleanField("sorted", sort.isSorted());
        json.writeBooleanField("unsorted", sort.isUnsorted());
        json.writeEndObject();
    }
}
