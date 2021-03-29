package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents one of the results from '/webservices/rest/documentType' DocFinity REST API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class DocumentTypeResponse {
    private String categoryId;
    private String categoryName;
    private @NonNull String id;
    private String name;
}
