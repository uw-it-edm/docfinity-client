package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/** Represents the response from '/webservices/rest/documentType' DocFinity REST API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentTypesResponse {
    private int totalAvailable;
    private List<DocumentTypeResponse> results;

    /** Helper method used by unit tests to simplify the creation of types. */
    public static DocumentTypesResponse from(String... documentTypeIds) {
        DocumentTypesResponse response = new DocumentTypesResponse();
        response.setTotalAvailable(documentTypeIds.length);
        List<DocumentTypeResponse> results =
                Arrays.stream(documentTypeIds)
                        .map(id -> new DocumentTypeResponse(id))
                        .collect(Collectors.toList());

        response.setResults(results);
        return response;
    }
}
