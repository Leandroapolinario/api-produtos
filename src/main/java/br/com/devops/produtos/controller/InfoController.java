package br.com.devops.produtos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint usado como evidência do deploy automático:
 * a cada push na main, "versao" e "commit" mudam após o Watchtower atualizar o container.
 */
@RestController
public class InfoController {

    private final Instant iniciadoEm = Instant.now();

    @Value("${app.versao}")
    private String versao;

    @Value("${app.commit}")
    private String commit;

    @Value("${app.mensagem}")
    private String mensagem;

    @GetMapping({"/", "/api/info"})
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("aplicacao", "api-produtos");
        info.put("mensagem", mensagem);
        info.put("versao", versao);
        info.put("commit", commit);
        info.put("iniciadoEm", iniciadoEm.toString());
        return info;
    }
}
