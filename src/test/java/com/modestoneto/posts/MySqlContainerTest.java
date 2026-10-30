package com.modestoneto.posts;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Um único MySQL 8.0 (a mesma imagem do docker-compose) para todos os testes que usam banco.
 * Sobe uma vez por execução e é removido ao fim pelo Ryuk do Testcontainers.
 */
public abstract class MySqlContainerTest {

    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0");

    static {
        MYSQL.start();
    }
}
