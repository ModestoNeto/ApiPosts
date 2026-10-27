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
| Antes (`findAll` padrão, tudo LAZY) | **10** (1 de posts + 6 de comentários + 3 de autores) |
| Depois (`@EntityGraph` com `author`, `comments`, `comments.author`) | **1** |

A correção fica no repositório do adaptador, sem tocar no domínio. O `@EntityGraph` com coleção não deve ser combinado com paginação: o Hibernate pagina em memória (tratado na paginação).

## Endpoints
| Método | Rota | Resposta |
|---|---|---|
| GET | `/users` | 200, lista de usuários |
| GET | `/users/{id}` | 200, ou 404 |
| POST | `/users` | 201, ou 400 (validação) e 409 (e-mail repetido) |
| PUT | `/users/{id}` | 200, ou 404 e 409 |
| DELETE | `/users/{id}` | 204, ou 404 e 409 (usuário com posts ou comentários) |
| POST | `/posts` | 201 com `Location`, ou 400 e 404 (autor inexistente) |
| GET | `/posts/{id}` | 200 com autor e comentários, ou 404 |
| POST | `/posts/{id}/comments` | 201 com o post inteiro, ou 400 e 404 (post ou autor inexistente) |

Erros seguem o formato `{timestamp, status, error, message, path}`.
