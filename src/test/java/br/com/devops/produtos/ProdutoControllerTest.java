package br.com.devops.produtos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mvc;

    private static final String NOVO_PRODUTO = """
            {"nome":"Teclado","descricao":"Teclado mecânico","preco":299.90,"estoque":20}
            """;

    @Test
    void deveListarProdutosIniciais() throws Exception {
        mvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Notebook"));
    }

    @Test
    void deveBuscarProdutoPorId() throws Exception {
        mvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        mvc.perform(get("/api/produtos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("999")));
    }

    @Test
    void deveCriarProduto() throws Exception {
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(NOVO_PRODUTO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/produtos/3")))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Teclado"));
    }

    @Test
    void deveRejeitarProdutoInvalido() throws Exception {
        String invalido = """
                {"nome":"","preco":0,"estoque":-1}
                """;
        mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.preco").exists())
                .andExpect(jsonPath("$.erros.estoque").exists());
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        mvc.perform(put("/api/produtos/2").contentType(MediaType.APPLICATION_JSON).content(NOVO_PRODUTO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Teclado"));
    }

    @Test
    void deveRemoverProduto() throws Exception {
        mvc.perform(delete("/api/produtos/1")).andExpect(status().isNoContent());
        mvc.perform(get("/api/produtos/1")).andExpect(status().isNotFound());
    }

    @Test
    void deveExporInfoEHealth() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacao").value("api-produtos-01251069"))
                .andExpect(jsonPath("$.versao").exists());
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
