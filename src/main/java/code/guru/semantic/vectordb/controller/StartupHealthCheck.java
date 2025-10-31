/* code-guru-semantic-search-service
 * Copyright (C) 2025 Srijan Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details:
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 */
package code.guru.semantic.vectordb.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class StartupHealthCheck {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.embedding.model}")
    private String embeddingModel;

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String qdrantHost;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int qdrantPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    public void checkServicesOnStartup() {
        log.info("========================================");
        log.info("Performing startup health checks...");
        log.info("========================================");

        checkOllama();
        checkQdrant();

        log.info("========================================");
        log.info("Health checks completed");
        log.info("========================================");
    }

    private void checkOllama() {
        try {
            log.info("Checking Ollama connection at: {}", ollamaBaseUrl);
            
            String tagsUrl = ollamaBaseUrl + "/api/tags";
            var response = restTemplate.getForEntity(tagsUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Ollama is running and accessible");
                log.info("  Response: {}", response.getBody());
                
                // Check if the embedding model is available
                if (response.getBody() != null && response.getBody().contains(embeddingModel)) {
                    log.info("✓ Embedding model '{}' is available", embeddingModel);
                } else {
                    log.warn("⚠ Embedding model '{}' might not be available", embeddingModel);
                    log.warn("  Run: ollama pull {}", embeddingModel);
                }
            }
        } catch (Exception e) {
            log.error("✗ Failed to connect to Ollama at {}", ollamaBaseUrl);
            log.error("  Error: {}", e.getMessage());
            log.error("  Please ensure Ollama is running:");
            log.error("    1. Check if Ollama is installed");
            log.error("    2. Start Ollama service");
            log.error("    3. Verify it's accessible at: {}", ollamaBaseUrl);
            log.error("    4. Test with: curl {}/api/tags", ollamaBaseUrl);
        }
    }

    private void checkQdrant() {
        try {
            log.info("Checking Qdrant connection at: {}:{}", qdrantHost, qdrantPort);
            
            String healthUrl = String.format("http://%s:6333/healthz", qdrantHost);
            var response = restTemplate.getForEntity(healthUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Qdrant is running and accessible");
            }
        } catch (Exception e) {
            log.error("✗ Failed to connect to Qdrant at {}:{}", qdrantHost, qdrantPort);
            log.error("  Error: {}", e.getMessage());
            log.error("  Please ensure Qdrant is running:");
            log.error("    1. Start Qdrant: docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant");
            log.error("    2. Verify it's accessible at: http://{}:6333/health", qdrantHost);
        }
    }
}
