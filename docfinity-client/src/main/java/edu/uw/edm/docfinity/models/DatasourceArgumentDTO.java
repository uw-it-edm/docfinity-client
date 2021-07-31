package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.uw.edm.docfinity.MetadataTypeEnum;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Represents a datasource argument included in the '/indexing/executeDatasource' request. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class DatasourceArgumentDTO {
    private final @NonNull String name;
    private final Object value;
    private final @NonNull MetadataTypeEnum dataType;
}
