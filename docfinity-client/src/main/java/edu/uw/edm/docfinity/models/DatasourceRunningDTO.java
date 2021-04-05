package edu.uw.edm.docfinity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
* Represents the 'EntryControlWrapperDTO' request data to 'webservices/rest/indexing/controls'
* DocFinity REST API.
*/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class DatasourceRunningDTO {
    private @NonNull String documentTypeId;
    private @NonNull String documentId;
    private @NonNull List<DocumentIndexingMetadataDTO> data;
    private boolean reindexFlag;
    private boolean clearMetadata;
}
