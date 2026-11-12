package com.brandon.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.brandon.finance.expense.enums.PeriodType;

import jakarta.validation.constraints.DecimalMin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseFilterRequest {
    @io.swagger.v3.oas.annotations.media.Schema(description = "Trecho literal, sem distinção de caixa/acentos; espaços externos removidos; vazio não restringe")
    private String description;
    @Schema(description = "IDs positivos, separados por vírgula (categoryIds=1,2) ou repetidos "
        + "(categoryIds=1&categoryIds=2). Seleciona qualquer uma das categorias.")
    private List<@Positive Long> categoryIds;
    @Schema(description = "TODAY: hoje; LAST_7_DAYS: hoje e seis dias anteriores; CURRENT_MONTH: mês completo. "
        + "Substituem as datas informadas. CUSTOM exige ambas as datas.")
    private PeriodType periodType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Data inicial inclusiva em formato ISO. Aceita limite isolado sem periodType.", example = "2026-01-01")
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Data final inclusiva, igual ou posterior à inicial.", example = "2026-01-31")
    private LocalDate endDate;

    @DecimalMin("0.0")
    @Schema(description = "Valor mínimo inclusivo, não negativo.", example = "10.50")
    private BigDecimal minAmount;

    @DecimalMin("0.0")
    @Schema(description = "Valor máximo inclusivo, não negativo e igual ou superior ao mínimo.", example = "50.00")
    private BigDecimal maxAmount;
}
