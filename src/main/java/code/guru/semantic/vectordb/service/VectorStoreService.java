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
package code.guru.semantic.vectordb.service;

import code.guru.semantic.vectordb.model.CodeChunk;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    /**
     * Load code chunks from JSON file and store in vector database
     */
    public void loadCodeChunksFromFile(String filePath) throws IOException {
        log.info("Loading code chunks from file: {}", filePath);

        try {
            List<CodeChunk> chunks = objectMapper.readValue(
                new File(filePath),
                new TypeReference<List<CodeChunk>>() {}
            );
            
            storeCodeChunks(chunks);
            log.info("Successfully loaded {} code chunks", chunks.size());
            
        } catch (Exception e) {
            log.error("Error loading code chunks from file: {}", e.getMessage());
        }
    }

    /**
     * Store code chunks in vector database
     */
    public void storeCodeChunks(List<CodeChunk> chunks) {
        List<Document> documents = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            CodeChunk chunk = chunks.get(i);
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("className", chunk.getClassName());
            metadata.put("methodName", chunk.getMethodName());
            metadata.put("returnType", chunk.getReturnType());
            metadata.put("chunkIndex", i);
            
            if (chunk.getParameters() != null) {
                metadata.put("parameters", String.join(", ", chunk.getParameters()));
            }
            
            // Create document with embedding text and metadata using UUID
            Document document = new Document(
                UUID.randomUUID().toString(),
                chunk.toEmbeddingText(),
                metadata
            );
            
            documents.add(document);
        }
        
        // Store in vector database
        vectorStore.add(documents);
        log.info("Stored {} documents in vector database", documents.size());
    }

    /**
     * Perform semantic search on code chunks
     */
    public List<Document> semanticSearch(String query, int topK) {
        log.debug("Performing semantic search for query: {}", query);
        
        SearchRequest searchRequest = SearchRequest.query(query)
            .withTopK(topK);
        
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.debug("Found {} results", results.size());
        
        return results;
    }

    /**
     * Search with similarity threshold
     */
    public List<Document> semanticSearchWithThreshold(String query, int topK, double threshold) {
        log.debug("Performing semantic search with threshold {} for query: {}", threshold, query);
        
        SearchRequest searchRequest = SearchRequest.query(query)
            .withTopK(topK)
            .withSimilarityThreshold(threshold);
        
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.debug("Found {} results above threshold", results.size());
        
        return results;
    }
}
