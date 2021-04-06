package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uw.edm.docfinity.MetadataTypeEnum;
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
    private MetadataTypeEnum metadataType = MetadataTypeEnum.STRING_VARIABLE;

    @JsonProperty("isRequired")
    private boolean isRequired;

    public DocumentTypeMetadataDTO(String metadataId, String metadataName, boolean required) {
        this(metadataId, metadataName);
        this.isRequired = required;
    }
}
