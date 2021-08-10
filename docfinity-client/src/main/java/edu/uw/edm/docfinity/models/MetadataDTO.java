package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uw.edm.docfinity.MetadataTypeEnum;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents a metadata entry included in the '/indexing/controls' response. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class MetadataDTO {
    @NonNull private String id;
    @NonNull private String name;
    private MetadataTypeEnum dataType = MetadataTypeEnum.STRING;
    private List<String> responsibilityMapping;

    @JsonProperty("required")
    private boolean isRequired;

    @JsonProperty("runDatasource")
    private boolean isRunDatasourceEnabled;

    @JsonProperty("allowMultipleValues")
    private boolean allowMultipleValues;

    @JsonProperty("parameterPromptDatasourceArguments")
    private List<DatasourceArgumentPromptDTO> datasourcePrompts;
}
