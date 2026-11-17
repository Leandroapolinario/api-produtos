# API Produtos — CI/CD com GitHub Actions, Docker, Watchtower e AWS EC2

API REST em **Java 21 + Spring Boot 3.5** com pipeline completo de Integração e Entrega Contínua:

```
GitHub (PR aprovado → main)
   └─▶ GitHub Actions ── job build ─▶ job test ─▶ job docker (build + push)
                                                                         │
                                                         Docker Hub (:latest)
                                                                         │
                                         Watchtower (na EC2, verifica a cada 30s)
                                                                         │
                                              EC2: container api-produtos-01251069 atualizado
```

## Identificação

- **RA:** 01251069
- **Nomes dos serviços (com RA):**

| Serviço | Nome |
|---|---|
| Instância EC2 | `ec2-publica-01251069` |
| Security Group | `sg-api-produtos-01251069` |
| Imagem no Docker Hub | `<usuario>/api-produtos-01251069` |
| Container da API | `api-produtos-01251069` |
| Container do Watchtower | `watchtower-01251069` |

---

## 1. A API

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/` ou `/api/info` | Versão, commit e mensagem da versão em execução (usado como evidência do deploy) |
| GET | `/api/produtos` | Lista produtos |
| GET | `/api/produtos/{id}` | Busca produto por id (404 se não existir) |
| POST | `/api/produtos` | Cria produto (201 + header `Location`) |
| PUT | `/api/produtos/{id}` | Atualiza produto |
| DELETE | `/api/produtos/{id}` | Remove produto (204) |
| GET | `/actuator/health` | Health check |

Exemplo de corpo para POST/PUT:

```json
{ "nome": "Teclado", "descricao": "Teclado mecânico", "preco": 299.90, "estoque": 20 }
```

Validações: `nome` obrigatório, `preco` > 0, `estoque` ≥ 0 (erros retornam 400 em formato `ProblemDetail`).
Os dados ficam em memória (o foco da atividade é o pipeline).

### Rodar localmente

```bash
mvn test                 # 12 testes (MockMvc + unitários)
mvn spring-boot:run      # http://localhost:8080
```


## 2. Estrutura do repositório

```
.github/workflows/ci.yml   Pipeline: jobs build → test → docker (build + push no Docker Hub)
Dockerfile                 Build multi-stage (Maven → JRE 21)
infra/setup-ec2.sh         Instala Docker na EC2 (Ubuntu) e sobe a API + Watchtower
src/main/java/...          Código da API
src/test/java/...          Testes automatizados
```

---

## 3. Passo a passo (segue a ordem da Aula 13)

### Parte 1 — CI

**1.1 Repositório no GitHub.** Crie o repositório *sem README* e, na pasta do projeto:
```bash
git init
git add .
git commit -m "feat: api de produtos com pipeline de CI"
git branch -M main
git remote add origin https://github.com/<seu-usuario>/api-produtos.git
git push -u origin main
```
Na aba **Actions**, confira os jobs `build` e `test` verdes. O job `docker` vai falhar
nessa primeira execução porque os secrets ainda não existem; isso é esperado.

**1.2 Token do Docker Hub.** Em <https://hub.docker.com>: *Account settings → Personal access tokens →
Generate new token*, permissão **Read & Write**. Copie o token.
Crie também um repositório **público** chamado `api-produtos`.

**1.3 GitHub Secrets.** No repositório: *Settings → Secrets and variables → Actions → New repository secret*:

| Nome | Valor |
|---|---|
| `DOCKERHUB_USERNAME` | seu usuário do Docker Hub |
| `DOCKERHUB_TOKEN` | o token gerado |

> No slide do código aparece `DOCKERHUB_USER`, mas o slide seguinte cadastra `DOCKERHUB_USERNAME`.
> Este projeto usa **`DOCKERHUB_USERNAME`** nos dois lugares.

**1.4 Branch develop:**
```bash
git checkout -b develop
git push -u origin develop
```

**1.5 Proteção das branches.** *Settings → Branches → Add branch protection rule* (ou *Add classic branch protection rule*), para `main` e depois para `develop`:
- ✅ Require a pull request before merging → ✅ Require approvals (1)
- ✅ Require status checks to pass before merging → ✅ Require branches to be up to date → adicione `build` e `test`
- ✅ Do not allow bypassing the above settings

> Fazendo a atividade sozinho? Com "Require approvals" você não consegue aprovar o próprio PR.
> Peça a um colega para aprovar, ou deixe o número de aprovações em 0 e mantenha só os status checks.

**1.6 Fluxo com Pull Request** (Conventional Commits):
```bash
git checkout develop
git checkout -b feat/minha-alteracao
# ... altera o código ...
git commit -am "feat: descricao da alteracao"
git push -u origin feat/minha-alteracao
```
Abra o PR `feat/... → develop`: o pipeline roda `build` + `test`. Depois do merge, abra o PR
`develop → main`. Ao fazer o merge na `main`, o job `docker` publica a imagem no Docker Hub.

### Parte 2 — EC2 + Watchtower (CD)

**2.1 Criar a instância.** Console AWS → *EC2 → Launch instance*:
- AMI **Ubuntu Server 24.04**, tipo `t2.micro`/`t3.micro`
- VPC padrão, subnet pública, *Auto-assign public IP* = **Enable**
- Security Group (entrada): **SSH 22** (My IP) e **Custom TCP 8080** (0.0.0.0/0)

**2.2 Configurar Docker + API + Watchtower.** Conecte por SSH e rode o script:
```bash
ssh -i sua-chave.pem ubuntu@<IP-PUBLICO>
curl -fsSLO https://raw.githubusercontent.com/<seu-usuario>/api-produtos/main/infra/setup-ec2.sh
bash setup-ec2.sh <seu-usuario-dockerhub>
```
O script executa os comandos do slide (`apt install docker.io`, `usermod -a -G docker`) e sobe dois containers:
- `api-produtos-01251069` na porta 8080, com a label que o Watchtower monitora
- `watchtower-01251069`, que consulta o Docker Hub a cada 30 s e recria a API quando a tag `latest` muda

Teste: `http://<IP-PUBLICO>:8080/api/info` e `http://<IP-PUBLICO>:8080/actuator/health`.

> **Por que Watchtower e não o self-hosted runner do slide?** O enunciado da atividade pede o fluxo
> GitHub → Actions → Registry → **Watchtower** → EC2. Não use os dois ao mesmo tempo: o `cd.yml` do slide
> recria o container com outro nome e sem a label, e o Watchtower deixa de atualizá-lo.
>
> Usamos `nickfedor/watchtower`, fork mantido do Watchtower. O `containrrr/watchtower` original foi
> arquivado em dez/2025 e falha no Docker 29+ com `client version 1.25 is too old`.

---

## 4. Demonstração e evidências

1. **Antes:** abra `http://<IP-PUBLICO>:8080/api/info` → `"mensagem": "Versao 1 - ..."`. Print do navegador.
2. **Alteração:** numa branch `feat/v2`, mude em `src/main/resources/application.properties`:
   ```properties
   app.mensagem=Versao 2 - Deploy automatico com Watchtower!
   ```
   PR para `develop` → merge → PR para `main` → merge.
3. **Pipeline:** aba *Actions* com `build`, `test` e `docker` verdes. Print.
4. **Registry:** Docker Hub com a nova tag `1.0.<n>` e o `latest` atualizado. Print.
5. **Watchtower:** na EC2, sem rodar nenhum comando de deploy:
   ```bash
   docker logs watchtower-01251069 --since 5m
   # Found new <usuario>/api-produtos-01251069atest image (sha256:...)
   # Stopping /api-produtos-01251069 ...
   # Creating /api-produtos-01251069
   docker ps
   ```
   Print do terminal.
6. **Depois:** recarregue `/api/info` → nova `mensagem`, `versao` e `commit`. Print do navegador.

Entrega pedida no material: **URL do repositório** + **print da execução na EC2 com terminal e navegador**.

---

## 5. Problemas comuns

| Sintoma | Causa provável |
|---|---|
| Job `docker` falha no login | Secrets com nome diferente de `DOCKERHUB_USERNAME`/`DOCKERHUB_TOKEN`, ou token sem permissão de escrita |
| `invalid reference format` | Secret `DOCKERHUB_USERNAME` vazio ou com letras maiúsculas |
| Navegador não abre a API | Porta 8080 não liberada no Security Group, ou usando `https://` |
| Watchtower não atualiza | Container sem a label, repositório privado no Docker Hub, ou imagem `containrrr/watchtower` |
| `permission denied` no docker | Rode `newgrp docker` ou saia e entre de novo no SSH |
| Não consigo fazer merge do PR | Regra exige 1 aprovação e você é o autor (ver nota em 1.5) |

> Ao terminar, **pare ou encerre a instância EC2** para não gerar custos.
