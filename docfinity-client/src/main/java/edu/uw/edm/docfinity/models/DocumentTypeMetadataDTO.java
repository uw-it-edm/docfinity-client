package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents the response from 'webservices/rest/documentType/metadata' DocFinity REST API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class DocumentTypeMetadataDTO {
    private @NonNull String metadataId;
    private @NonNull String metadataName;
    private String metadataType;
    private boolean isRequired;
}
