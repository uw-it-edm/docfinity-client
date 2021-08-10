package edu.uw.edm.docfinity;

import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.MetadataDTO;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/** Encapsulates arguments for invoking a datasource. */
@Data
@NoArgsConstructor
public class ExecuteDatasourceArgs {
    private @NonNull String documentId;
    private @NonNull String documentTypeId;
    private @NonNull Multimap<String, Object> clientFields;
    private @NonNull Map<String, MetadataDTO> metadataMap;
    private @NonNull String documentTypeName;
    private @NonNull String category;
}
