package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    /**
    * Note: DocFinity will return this property with different types: 1) null (the field is not set).
    * 2) string. 3) number (for decimal, integers and dates). 4) array of strings (for
    * multi-selection fields). The property is deserialized into an Object[] (regardless if it is
    * single or multi-select), which makes it simpler to flatten the whole list of fields to send to
    * DocFinity.
    */
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private Object[] strDefaultValue;

    /** Constructs a new medata object with single select value. */
    public DocumentServerMetadataDTO(String id, String name, Object value) {
        this.id = id;
        this.name = name;

        if (value != null) {
            this.strDefaultValue = new Object[] {value};
        }
    }
}
