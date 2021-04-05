package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
* Represents the 'ParameterPromptDTO2 response from the 'webservices/rest/indexing/controls'
* DocFinity REST API.
*/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class DocumentServerMetadataDTO {
    private @NonNull String id;
    private @NonNull String name;
    private @NonNull Object strDefaultValue;
}
