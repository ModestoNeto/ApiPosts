INSERT INTO posts (id, author_id, title, body, created_at) VALUES
  (1, 1, 'Primeiros passos com Spring Boot', 'Criei meu primeiro projeto com o Initializr e subi o MySQL no Docker.', '2026-02-01 08:00:00.000000'),
  (2, 1, 'Flyway na prática',                'Versionar o esquema do banco evita muita dor de cabeça.',              '2026-02-03 09:30:00.000000'),
  (3, 2, 'Arquitetura hexagonal',            'Separar o domínio dos adaptadores deixa o núcleo fácil de testar.',    '2026-02-05 14:00:00.000000'),
  (4, 2, 'Entendendo o problema N+1',        'Uma consulta para a lista e mais uma para cada item: o custo cresce.', '2026-02-07 10:15:00.000000'),
  (5, 3, 'JPQL ou SQL nativo?',              'Cada um tem o seu lugar; o importante é isolar no adaptador.',         '2026-02-09 16:45:00.000000'),
  (6, 3, 'Paginação sem dor',                'Tipos próprios de página mantêm a aplicação livre do Spring Data.',    '2026-02-11 11:20:00.000000');

INSERT INTO comments (post_id, author_id, text, created_at) VALUES
  (1, 2, 'Ótimo começo!',                              '2026-02-01 10:00:00.000000'),
  (1, 3, 'Também uso o Docker para o banco.',          '2026-02-01 11:30:00.000000'),
  (2, 3, 'Migração aplicada não se edita, né?',        '2026-02-03 12:00:00.000000'),
  (2, 2, 'Salvou meu projeto mais de uma vez.',        '2026-02-03 13:10:00.000000'),
  (3, 1, 'Gostei da regra de dependência.',            '2026-02-05 15:00:00.000000'),
  (3, 3, 'O ArchUnit ajuda a garantir isso.',          '2026-02-05 16:20:00.000000'),
  (4, 1, 'Já caí nessa armadilha.',                    '2026-02-07 11:00:00.000000'),
  (4, 3, 'O JOIN FETCH resolve na maioria dos casos.', '2026-02-07 12:40:00.000000'),
  (5, 1, 'Nativo só quando precisa mesmo.',            '2026-02-09 17:30:00.000000'),
  (5, 2, 'Concordo.',                                  '2026-02-09 18:00:00.000000'),
  (6, 1, 'Ordenação só por campos permitidos.',        '2026-02-11 12:30:00.000000'),
  (6, 2, 'Boa dica sobre o Pageable.',                 '2026-02-11 13:15:00.000000');
