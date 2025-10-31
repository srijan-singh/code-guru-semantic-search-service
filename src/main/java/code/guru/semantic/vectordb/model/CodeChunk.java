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
package code.guru.semantic.vectordb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeChunk {
    
    @JsonProperty("className")
    private String className;
    
    @JsonProperty("docs")
    private String docs;
    
    @JsonProperty("methodName")
    private String methodName;
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("parameters")
    private List<String> parameters;
    
    @JsonProperty("classAttributes")
    private List<String> classAttributes;
    
    @JsonProperty("calledBy")
    private List<String> calledBy;
    
    @JsonProperty("dependencies")
    private List<String> dependencies;
    
    @JsonProperty("methodCode")
    private String methodCode;
    
    public String toEmbeddingText() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Class: ").append(className).append("\n");
        sb.append("Method: ").append(methodName).append("\n");
        sb.append("Return Type: ").append(returnType).append("\n");
        
        if (docs != null && !docs.isEmpty()) {
            sb.append("Documentation: ").append(docs).append("\n");
        }
        
        if (parameters != null && !parameters.isEmpty()) {
            sb.append("Parameters: ").append(String.join(", ", parameters)).append("\n");
        }
        
        if (dependencies != null && !dependencies.isEmpty()) {
            sb.append("Dependencies: ").append(String.join(", ", dependencies)).append("\n");
        }
        
        if (methodCode != null && !methodCode.isEmpty()) {
            sb.append("Code:\n").append(methodCode);
        }
        
        return sb.toString();
    }
}
