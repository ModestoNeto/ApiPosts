# API de Posts e Usuários

API REST de uma mini rede social (usuários, posts e comentários) com Spring Boot e MySQL, organizada em arquitetura hexagonal.

> Em construção. Este README cresce junto com o projeto.

## Tecnologias
- Java 21, Spring Boot 4.1
- Spring Web MVC, Spring Data JPA, Hibernate
- MySQL 8 (Docker), Flyway
- Bean Validation, Maven

## Como rodar
Pré-requisitos: JDK 21 ou superior, Docker Desktop e Git.

1. Clone o repositório e entre na pasta.
2. Crie o arquivo de variáveis a partir do modelo:
   ```
   copy .env.example .env
   ```
   Abra o `.env` e preencha `DB_ROOT_PASSWORD` e `DB_PASSWORD`. Use letras e números, sem `$ # = "`. O `.env` nunca vai para o Git.
3. Suba o MySQL (porta `3308`):
   ```
   docker compose up -d --wait
   ```
4. Rode a aplicação:
   ```
   .\mvnw.cmd spring-boot:run
   ```
5. Para os testes (o MySQL precisa estar de pé):
   ```
   .\mvnw.cmd test
   ```

## Arquitetura
Hexagonal: o núcleo não conhece frameworks, e o que é específico de JPA fica no adaptador de persistência.

```
com.modestoneto.posts
 ├─ domain               modelos e exceções, sem Spring ou JPA
 ├─ application          portas (in/out), serviços e tipos de paginação próprios
 ├─ adapter
 │   ├─ in.web           controllers, DTOs e tratamento de erros
 │   └─ out.persistence  entidades JPA, repositórios e adaptadores
 └─ config
```

## Origem e créditos
Projeto de estudo baseado no curso "Java COMPLETO" (Udemy). O curso usa arquitetura em camadas; aqui o domínio e os casos de uso são os mesmos, mas a estrutura foi **adaptada para arquitetura hexagonal**. Esta é, portanto, uma versão adaptada, não uma cópia do projeto original.

## Decisões de persistência
- `@ManyToOne(fetch = LAZY)` em todas as associações, com `spring.jpa.open-in-view=false`: a entidade JPA nunca sai do adaptador. O mapper converte para o domínio dentro da transação.
- `getReferenceById` para ligar post e comentário ao autor sem `SELECT` extra (o serviço já validou que o autor existe).
- Comentários em `Post` com `cascade = ALL` e `orphanRemoval = true`: o domínio é imutável e o `save` faz `merge` da lista inteira.

## N+1: antes e depois
Listagem de 6 posts com 13 comentários e seus autores, medida pelo log SQL do Hibernate:

| | Queries por listagem |
|---|---|
| Antes (listagem padrão, tudo LAZY) | **10** (1 de posts + 6 de comentários + 3 de autores) |
| Depois (`@EntityGraph` com `author`, `comments`, `comments.author`) | **1** |

A correção fica no repositório do adaptador, sem tocar no domínio.

## Endpoints
| Método | Rota | Resposta |
|---|---|---|
| GET | `/users?page=&size=&sort=` | 200, página de usuários (`sort`: `name` ou `createdAt`, padrão `createdAt,desc`; `size` de 1 a 100), ou 400 |
| GET | `/users/{id}` | 200, ou 404 |
| POST | `/users` | 201, ou 400 (validação) e 409 (e-mail repetido) |
| PUT | `/users/{id}` | 200, ou 404 e 409 |
| DELETE | `/users/{id}` | 204, ou 404 e 409 (usuário com posts ou comentários) |
| POST | `/posts` | 201 com `Location`, ou 400 e 404 (autor inexistente) |
| GET | `/users/{id}/posts?page=&size=&sort=` | 200, página de posts com comentários (`sort`: `title` ou `createdAt`), ou 400 e 404 |
| GET | `/posts/{id}` | 200 com autor e comentários, ou 404 |
| POST | `/posts/{id}/comments` | 201 com o post inteiro, ou 400 e 404 (post ou autor inexistente) |

Erros seguem o formato `{timestamp, status, error, message, path}`.

## Paginação com coleção
`GET /users/{id}/posts` pagina posts que carregam comentários. A preocupação clássica é que `JOIN FETCH` de coleção com `Pageable` faça o Hibernate paginar **em memória** (aviso `HHH90003004`). Foi testado nesta versão (Hibernate 7.4.5) e **não acontece**: o `LIMIT` vai para uma subconsulta sobre `posts` e os comentários entram por `LEFT JOIN`, e cada página traz o post com todos os comentários. Por isso o `@EntityGraph` basta, sem a técnica de duas consultas.

| Página de 2 posts e 5 comentários | Queries |
|---|---|
| Sem fetch (tudo LAZY) | **7** |
| `@EntityGraph(author, comments, comments.author)` | **2** (usuário + posts com joins) |

Em versões anteriores do Hibernate o comportamento pode ser outro. Vale repetir a verificação no log ao atualizar.
