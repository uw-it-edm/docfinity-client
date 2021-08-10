package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents a metadata entry for the request AND response to the '/indexing/index/commit' API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class DocumentIndexingMetadataDTO {
    private @NonNull String id;
    private @NonNull String metadataId;
    private @NonNull String metadataName;
    private Object value;

    @JsonProperty("markedForDelete")
    private boolean markedForDelete;

    public DocumentIndexingMetadataDTO(
            String id, String metadataId, String metadataName, Object value) {
        this.id = id;
        this.metadataId = metadataId;
        this.metadataName = metadataName;
        this.value = value;
    }
}
