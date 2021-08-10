package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

/** Represents the request to the '/indexing/controls' API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentControlsRequestDTO {
    private @NonNull String documentTypeId;
    private @NonNull String documentId;
}
