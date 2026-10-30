package com.modestoneto.posts.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("API de Posts e Usuários")
                .version("1.0.0")
                .description("Mini rede social com usuários, posts e comentários. Erros seguem o formato "
                        + "{timestamp, status, error, message, path}."));
    }
}
