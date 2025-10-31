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

import code.guru.semantic.vectordb.dto.SearchRequest;
import code.guru.semantic.vectordb.dto.SearchResponse;
import code.guru.semantic.vectordb.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vector")
@RequiredArgsConstructor
@Slf4j
public class VectorStoreController {

    private final VectorStoreService vectorStoreService;

    /**
     * Semantic search endpoint
     * POST /api/vector/search
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        log.info("Received search request: {}", request.getQuery());
        
        List<Document> documents;
        
        if (request.getThreshold() != null) {
            documents = vectorStoreService.semanticSearchWithThreshold(
                request.getQuery(),
                request.getTopK(),
                request.getThreshold()
            );
        } else {
            documents = vectorStoreService.semanticSearch(
                request.getQuery(),
                request.getTopK()
            );
        }
        
        List<SearchResponse.SearchResult> results = documents.stream()
            .map(doc -> new SearchResponse.SearchResult(
                doc.getContent(),
                doc.getMetadata(),
                null // Score is embedded in metadata if available
            ))
            .collect(Collectors.toList());
        
        SearchResponse response = new SearchResponse(results, results.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Load code chunks from file endpoint
     * POST /api/vector/load?filePath=/path/to/file.json
     */
    @PostMapping("/load")
    public ResponseEntity<String> loadFromFile(@RequestParam String filePath) {
        try {
            log.info("Loading code chunks from file: {}", filePath);
            vectorStoreService.loadCodeChunksFromFile(filePath);
            return ResponseEntity.ok("Successfully loaded code chunks from " + filePath);
        } catch (Exception e) {
            log.error("Error loading code chunks", e);
            return ResponseEntity.badRequest()
                .body("Error loading code chunks: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vector Store Service is running");
    }
}
