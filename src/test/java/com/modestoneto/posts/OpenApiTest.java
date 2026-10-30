package com.modestoneto.posts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiTest extends MySqlContainerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void documentacaoOpenApiListaOsEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Posts e Usuários"))
                .andExpect(jsonPath("$.paths['/users']").exists())
                .andExpect(jsonPath("$.paths['/posts/search']").exists())
                .andExpect(jsonPath("$.components.schemas.StandardError").exists());
    }
}
