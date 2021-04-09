package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/** Represents the response from '/webservices/rest/documentType' DocFinity REST API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentTypeDTOSearchResult {
    private int totalAvailable = 0;
    private List<DocumentTypeDTO> results = new ArrayList<>();

    /** Helper method used by unit tests to simplify the creation of types. */
    public static DocumentTypeDTOSearchResult from(String... documentTypeIds) {
        DocumentTypeDTOSearchResult response = new DocumentTypeDTOSearchResult();
        response.setTotalAvailable(documentTypeIds.length);
        List<DocumentTypeDTO> results =
                Arrays.stream(documentTypeIds)
                        .map(id -> new DocumentTypeDTO(id))
                        .collect(Collectors.toList());

        response.setResults(results);
        return response;
    }
}
