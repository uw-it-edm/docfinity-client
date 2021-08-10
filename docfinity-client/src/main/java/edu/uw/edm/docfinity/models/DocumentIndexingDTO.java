package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents the request AND response to the '/indexing/index/commit' DocFinity REST API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class DocumentIndexingDTO {
    private @NonNull String documentTypeId;
    private @NonNull String documentId;

    @JsonProperty("documentIndexingMetadataDtos")
    private @NonNull List<DocumentIndexingMetadataDTO> indexingMetadata;

    private boolean metadataLoaded;
}
