package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
* Represents a data entry of the request to the 'webservices/rest/indexing/controls' DocFinity REST
* API.
*/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentIndexingMetadataDTO {
    private @NonNull String metadataId;
    private Object value;
}
