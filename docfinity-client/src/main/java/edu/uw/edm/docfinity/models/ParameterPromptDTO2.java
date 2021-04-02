package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents the response from the 'webservices/rest/indexing/controls' DocFinity REST API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class ParameterPromptDTO2 {
    private @NonNull String id;
    private @NonNull String name;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private Object[] strDefaultValue;

    public ParameterPromptDTO2(String id, String name, Object value) {
        this.id = id;
        this.name = name;

        if (value != null) {
            this.strDefaultValue = new Object[] {value};
        }
    }
}
