package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
* Represents a datasource argument prompt definition included in the '/indexing/controls' response.
*/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class DatasourceArgumentPromptDTO {
    private @JsonProperty("datasourceArgumentName") @NotNull String argumentName;
    private String value;
    private String argumentType;
}
