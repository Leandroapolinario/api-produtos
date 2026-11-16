package br.com.devops.produtos.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 100, message = "nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "descricao deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "preco é obrigatório")
        @DecimalMin(value = "0.01", message = "preco deve ser maior que zero")
        BigDecimal preco,

        @NotNull(message = "estoque é obrigatório")
        @Min(value = 0, message = "estoque não pode ser negativo")
        Integer estoque
) {
    public Produto toProduto(Long id) {
        return new Produto(id, nome, descricao, preco, estoque);
    }
}
