package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Helper class to map data to and from DocFinity DTO's. */
@Data
@RequiredArgsConstructor
public class DocFinityDtoMapper {
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    private final CreateDocumentArgs args;

    /**
    * Builds and validates the indexing models from the user data and metadata definitions which are
    * used to execute datasources.
    *
    * @param metadataMap Map of metadata definitions by name.
    * @return List of indexing models that can be used to execute datasources.
    */
    public List<DocumentIndexingMetadataDTO> getPartialIndexingDtos(
            Map<String, DocumentTypeMetadataDTO> metadataMap) {
        Preconditions.checkNotNull(metadataMap, "metadataMap is required.");

        List<DocumentIndexingMetadataDTO> indexingDtos = new ArrayList<>();

        for (Map.Entry<String, Object> entry : args.getMetadata().entries()) {
            String metadataName = entry.getKey();
            DocumentTypeMetadataDTO metadata = metadataMap.get(metadataName);

            if (metadata == null) {
                throwMetadataDoesNotExistException(metadataName, metadataMap);
            }

            String metadataId = metadata.getMetadataId();
            Object metadataValue = getTypedMetadataValue(entry.getValue(), metadata);
            indexingDtos.add(new DocumentIndexingMetadataDTO(metadataId, metadataName, metadataValue));
        }

        return indexingDtos;
    }

    /**
    * Builds and validates the indexing models from the full list of metadata values from server
    * (from running datasources). The result can be used to execute the '/indexing/index/commit' REST
    * API.
    *
    * @param metadataMap Map of metadata definitions by name.
    * @param serverMetadataDtos List of metadata values from server after running datasources.
    * @return List of indexing models that can be used to index and commit a document.
    */
    public List<DocumentIndexingMetadataDTO> getFinalIndexingDtos(
            Map<String, DocumentTypeMetadataDTO> metadataMap,
            List<DocumentServerMetadataDTO> serverMetadataDtos) {

        Preconditions.checkNotNull(metadataMap, "metadataMap is required.");
        Preconditions.checkNotNull(serverMetadataDtos, "serverMetadataDtos is required.");

        List<DocumentIndexingMetadataDTO> indexMetadatas = new ArrayList<>();

        for (DocumentServerMetadataDTO serverDto : serverMetadataDtos) {
            Object[] values = serverDto.getStrDefaultValue();
            String metadataName = serverDto.getName();
            DocumentTypeMetadataDTO metadataDO = metadataMap.get(metadataName);
            boolean metadataRequired = metadataDO != null && metadataDO.isRequired();

            if (values != null) {
                for (Object value : values) {
                    if (metadataRequired && isNullOrEmpty(value)) {
                        throwMetadataRequiredException(metadataName);
                    }

                    // Note that for multi-selection fields an entry is added for each selection using the
                    // same field metadata id.
                    indexMetadatas.add(
                            new DocumentIndexingMetadataDTO(serverDto.getId(), metadataName, value));
                }
            } else if (metadataRequired) {
                throwMetadataRequiredException(metadataName);
            }
        }

        // TODO: Verify datasources

        return indexMetadatas;
    }

    private Object getTypedMetadataValue(Object value, DocumentTypeMetadataDTO metadata) {
        if (metadata.getMetadataType() == MetadataTypeEnum.DATE) {
            if (value instanceof String) {
                try {
                    return new SimpleDateFormat(DATE_FORMAT).parse((String) value);
                } catch (ParseException e) {
                    throwUnableToParseDate(value, metadata.getMetadataName(), e);
                }
            } else {
                throwUnableToParseDate(value, metadata.getMetadataName(), null);
            }
        } else if (metadata.getMetadataType() == MetadataTypeEnum.INTEGER
                && value instanceof Integer == false
                && value instanceof Long == false) {

            // Note: This case is validated because if client sends a decimal, DocFinity will silently
            // round to integer and is a potential data loss.
            String message =
                    String.format(
                            "Invalid integer value for metadata object '%s'. Type: %s. Value: %s",
                            metadata.getMetadataName(), value.getClass(), value);
            throw new IllegalStateException(message);
        }

        return value;
    }

    private void throwUnableToParseDate(Object value, String metadataName, Throwable e) {
        String message =
                String.format(
                        "Unable to parse value '%s' as date with format '%s' for metadata object '%s'.",
                        value, DATE_FORMAT, metadataName);
        throw new IllegalStateException(message, e);
    }

    private void throwMetadataRequiredException(String metadataName) {
        throw new IllegalStateException(
                String.format(
                        "Missing value for required metadata '%s' for document type '%s'.",
                        metadataName, args.getDocumentTypeName()));
    }

    private void throwMetadataDoesNotExistException(
            String metadataName, Map<String, DocumentTypeMetadataDTO> metadataMap) {
        throw new IllegalStateException(
                String.format(
                        "Document type '%s' is missing metadata object named '%s'. Available metadata: %s.",
                        args.getDocumentTypeName(), metadataName, String.join(", ", metadataMap.keySet())));
    }

    private boolean isNullOrEmpty(Object value) {
        if (value instanceof String) {
            return Strings.isNullOrEmpty((String) value);
        } else {
            return value == null;
        }
    }
}
