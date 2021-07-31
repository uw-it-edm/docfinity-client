package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents the response to the '/indexing/executeDatasource' API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteDatasourceResponseDTO {
    private String key;
    private Object value;

    public ExecuteDatasourceResponseDTO(Object value) {
        this.value = value;
    }
}
