package br.com.devops.produtos.model;

import java.math.BigDecimal;

public record Produto(Long id, String nome, String descricao, BigDecimal preco, Integer estoque) {

    public Produto comId(Long novoId) {
        return new Produto(novoId, nome, descricao, preco, estoque);
    }
}
