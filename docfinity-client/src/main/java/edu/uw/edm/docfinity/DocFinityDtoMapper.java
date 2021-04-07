package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Helper class to map data to and from DocFinity DTO's. */
@Data
@RequiredArgsConstructor
public class DocFinityDtoMapper {
    private final String documentTypeId;
    private final String documentId;
    private final Multimap<String, Object> clientMetadata;

    /** Builds the data to execute datasources from the given metadata object definitions. */
    public DatasourceRunningDTO buildDatasourceDtoFromMetadata(
            List<DocumentTypeMetadataDTO> metadataDtos) {
        Preconditions.checkNotNull(metadataDtos, "metadataDtos is required.");

        // TODO: Add validation and checks.
        Map<String, DocumentTypeMetadataDTO> metadataDefinitionsByName =
                metadataDtos.stream()
                        .collect(Collectors.toMap(DocumentTypeMetadataDTO::getMetadataName, m -> m));

        List<DocumentIndexingMetadataDTO> indexingMetadataDtos =
                clientMetadata.entries().stream()
                        .map(
                                entry -> {
                                    String metadataId = metadataDefinitionsByName.get(entry.getKey()).getMetadataId();
                                    return new DocumentIndexingMetadataDTO(metadataId, entry.getValue());
                                })
                        .collect(Collectors.toList());

        return new DatasourceRunningDTO(documentTypeId, documentId, indexingMetadataDtos);
    }

    /**
    * Builds the data to execute the '/indexing/index/commit' request from the given list of server
    * metadatga values.
    */
    public DocumentIndexingDTO buildIndexingDtoFromServerMetadataDtos(
            List<DocumentServerMetadataDTO> serverMetadataDtos) {
        Preconditions.checkNotNull(serverMetadataDtos, "serverMetadataDtos is required.");

        List<DocumentIndexingMetadataDTO> indexMetadatas = new ArrayList<>();

        for (DocumentServerMetadataDTO metadataDto : serverMetadataDtos) {
            Object[] values = metadataDto.getStrDefaultValue();

            // Note that for multi-selection fields an entry is added for each selection using the same
            // field metadata id.
            if (values != null) {
                for (Object value : values) {
                    indexMetadatas.add(new DocumentIndexingMetadataDTO(metadataDto.getId(), value));
                }
            }
        }

        return new DocumentIndexingDTO(documentTypeId, documentId, indexMetadatas);
    }
}
