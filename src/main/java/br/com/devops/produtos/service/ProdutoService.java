package br.com.devops.produtos.service;

import br.com.devops.produtos.exception.ProdutoNaoEncontradoException;
import br.com.devops.produtos.model.Produto;
import br.com.devops.produtos.model.ProdutoRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Armazenamento em memória para manter o foco da atividade no pipeline CI/CD.
 * Os dados são reiniciados a cada novo deploy (nova versão do container).
 */
@Service
public class ProdutoService {

    private final Map<Long, Produto> produtos = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public ProdutoService() {
        criar(new ProdutoRequest("Notebook", "Notebook 16GB RAM", new BigDecimal("4500.00"), 10));
        criar(new ProdutoRequest("Mouse", "Mouse sem fio", new BigDecimal("89.90"), 50));
    }

    public List<Produto> listar() {
        return produtos.values().stream()
                .sorted(Comparator.comparing(Produto::id))
                .toList();
    }

    public Produto buscar(Long id) {
        Produto produto = produtos.get(id);
        if (produto == null) {
            throw new ProdutoNaoEncontradoException(id);
        }
        return produto;
    }

    public Produto criar(ProdutoRequest request) {
        Long id = sequence.incrementAndGet();
        Produto produto = request.toProduto(id);
        produtos.put(id, produto);
        return produto;
    }

    public Produto atualizar(Long id, ProdutoRequest request) {
        buscar(id);
        Produto atualizado = request.toProduto(id);
        produtos.put(id, atualizado);
        return atualizado;
    }

    public void remover(Long id) {
        if (produtos.remove(id) == null) {
            throw new ProdutoNaoEncontradoException(id);
        }
    }
}
