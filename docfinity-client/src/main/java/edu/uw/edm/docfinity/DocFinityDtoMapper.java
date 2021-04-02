package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import edu.uw.edm.docfinity.models.EntryControlWrapperDTO;
import edu.uw.edm.docfinity.models.ParameterPromptDTO2;
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

    /**
    * Builds the data to execute a '/indexing/controls' request from the given metadata object
    * definitions.
    */
    public EntryControlWrapperDTO buildControlDtoFromMetadata(
            List<DocumentTypeMetadataDTO> metadataDefinitions) {
        Preconditions.checkNotNull(metadataDefinitions, "metadataDefinitions is required.");

        // TODO: Add validation and checks.
        Map<String, DocumentTypeMetadataDTO> metadataDefinitionsByName =
                metadataDefinitions.stream()
                        .collect(Collectors.toMap(DocumentTypeMetadataDTO::getMetadataName, m -> m));

        List<DocumentIndexingMetadataDTO> controlsRequest =
                clientMetadata.entries().stream()
                        .map(
                                entry -> {
                                    String metadataId = metadataDefinitionsByName.get(entry.getKey()).getMetadataId();
                                    return new DocumentIndexingMetadataDTO(metadataId, entry.getValue());
                                })
                        .collect(Collectors.toList());

        return new EntryControlWrapperDTO(documentTypeId, documentId, controlsRequest);
    }

    /**
    * Builds the data to execute the '/indexing/index/commit' request from the given list of control
    * prompts.
    */
    public DocumentIndexingDTO buildIndexingDtoFromControlPromptDtos(
            List<ParameterPromptDTO2> controlPrompts) {
        Preconditions.checkNotNull(controlPrompts, "controlPrompts is required.");

        List<DocumentIndexingMetadataDTO> indexMetadatas = new ArrayList<>();

        for (ParameterPromptDTO2 prompt : controlPrompts) {
            Object[] values = prompt.getStrDefaultValue();

            if (values != null) {
                for (Object value : values) {
                    indexMetadatas.add(new DocumentIndexingMetadataDTO(prompt.getId(), value));
                }
            }
        }

        return new DocumentIndexingDTO(documentTypeId, documentId, indexMetadatas);
    }
}
