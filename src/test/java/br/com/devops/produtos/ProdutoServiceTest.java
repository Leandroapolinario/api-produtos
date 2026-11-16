package br.com.devops.produtos;

import br.com.devops.produtos.exception.ProdutoNaoEncontradoException;
import br.com.devops.produtos.model.ProdutoRequest;
import br.com.devops.produtos.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoServiceTest {

    private ProdutoService service;

    @BeforeEach
    void setUp() {
        service = new ProdutoService();
    }

    @Test
    void deveIniciarComDoisProdutos() {
        assertThat(service.listar()).hasSize(2);
    }

    @Test
    void deveGerarIdsSequenciais() {
        var p = service.criar(new ProdutoRequest("Monitor", null, new BigDecimal("999.00"), 5));
        assertThat(p.id()).isEqualTo(3L);
        assertThat(service.listar()).hasSize(3);
    }

    @Test
    void deveLancarExcecaoAoAtualizarInexistente() {
        var req = new ProdutoRequest("X", null, BigDecimal.ONE, 1);
        assertThatThrownBy(() -> service.atualizar(42L, req))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoRemoverInexistente() {
        assertThatThrownBy(() -> service.remover(42L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }
}
