package com.brandon.finance.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;
    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 255, message = "Descrição deve ter até 255 caracteres")
    private String description;
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter até 8 dígitos inteiros e duas casas decimais")
    private BigDecimal amount;
    @NotNull(message = "Data é obrigatória")
    @Schema(description = "Data de calendário ISO yyyy-MM-dd, sem fuso horário")
    private LocalDate date;
    private Long userId;
    @Schema(description = "Categoria do usuário; null ou ausência remove o vínculo na edição", nullable = true)
    private Long categoryId;
    @Schema(description = "Conta do usuário; null ou ausência remove o vínculo na edição", nullable = true)
    private Long accountId;
    private String categoryName;

    public void setDescription(String description) {
        this.description = description == null ? null : description.strip();
    }
}
