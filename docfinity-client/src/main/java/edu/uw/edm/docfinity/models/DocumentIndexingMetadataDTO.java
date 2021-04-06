package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
* Represents a metadata entry for: 1) the request to the '/indexing/controls' API; 2) the request
* AND response to the '/indexing/index/commit' API.
*/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentIndexingMetadataDTO {
    private @NonNull String metadataId;
    private @NonNull String metadataName;
    private Object value;
}
