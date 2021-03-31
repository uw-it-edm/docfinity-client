package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
* Represents the request AND response to the 'webservices/rest/indexing/index/commit' DocFinity
* REST API.
*/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class DocumentIndexingDTO {
    private @NonNull String documentTypeId;
    private @NonNull String documentId;
    private @NonNull List<DocumentIndexingMetadataDTO> documentIndexingMetadataDtos;
    private boolean metadataLoaded;
}
