package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/** Represents the request to the '/indexing/executeDatasource' API. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ExecuteDatasourceRequestDTO {
    private @NonNull String documentId;
    private @NonNull String documentTypeId;
    private @NonNull String metadataId;
    private @NonNull List<DatasourceArgumentDTO> arguments;
}
