package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.MetadataDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Helper class that builds and validates the metadata information for indexing documents. */
public class IndexingMetadataBuilder {
    private final String documentTypeName;
    private final Map<String, MetadataDTO> metadataMap;
    private final Multimap<String, DocumentIndexingMetadataDTO> currentIndexingDtos;
    private final List<DocumentIndexingMetadataDTO> indexingDtos;

    public IndexingMetadataBuilder(
            String documentTypeName,
            Map<String, MetadataDTO> metadataMap,
            List<DocumentIndexingMetadataDTO> currentIndexingDtos) {
        Preconditions.checkNotNull(documentTypeName, "documentTypeName is required.");
        Preconditions.checkNotNull(metadataMap, "metadataMap is required.");
        Preconditions.checkNotNull(currentIndexingDtos, "currentIndexingDtos is required.");

        indexingDtos = new ArrayList<>();
        this.documentTypeName = documentTypeName;
        this.metadataMap = metadataMap;
        this.currentIndexingDtos =
                Multimaps.index(currentIndexingDtos, DocumentIndexingMetadataDTO::getMetadataId);
    }

    /** Adds an indexing field entry. */
    public IndexingMetadataBuilder addValue(DocumentField field) {
        Multimap<String, Object> map = ArrayListMultimap.create();
        map.putAll(field.getName(), field.getValues());
        return addValues(map);
    }

    /** Adds multiple indexing field entries. */
    public IndexingMetadataBuilder addValues(Multimap<String, Object> fields) {
        Preconditions.checkNotNull(fields, "fields is required.");

        for (Map.Entry<String, Collection<Object>> entry : fields.asMap().entrySet()) {
            String metadataName = entry.getKey();
            List<Object> metadataValues = new ArrayList<>(entry.getValue());
            MetadataDTO metadataDto = metadataMap.get(metadataName);

            if (metadataDto == null) {
                throwMetadataDoesNotExistException(metadataName, metadataMap);
            }

            if (!metadataDto.isAllowMultipleValues()) {
                DocumentIndexingMetadataDTO dto = getSingleValueIndexingDTO(metadataDto, metadataValues);
                indexingDtos.add(dto);

            } else {
                List<DocumentIndexingMetadataDTO> dtos =
                        getMultiValueIndexingDTO(metadataDto, metadataValues);
                indexingDtos.addAll(dtos);
            }
        }

        return this;
    }

    private DocumentIndexingMetadataDTO getSingleValueIndexingDTO(
            MetadataDTO metadataDto, List<Object> metadataValues) {

        String metadataId = metadataDto.getId();
        String metadataName = metadataDto.getName();

        if (metadataValues.size() > 1) {
            throwInvalidValuesForSingleSelectMetadata(metadataName);
        }

        Object metadataValue = getTypedMetadataValue(metadataValues.get(0), metadataDto);
        Optional<DocumentIndexingMetadataDTO> currentIndexingDto =
                currentIndexingDtos.get(metadataDto.getId()).stream().findFirst();
        String indexingDtoId = currentIndexingDto.isPresent() ? currentIndexingDto.get().getId() : null;
        DocumentIndexingMetadataDTO indexingDto =
                new DocumentIndexingMetadataDTO(indexingDtoId, metadataId, metadataName, metadataValue);

        if (indexingDtoId != null && isNullOrEmpty(metadataValue)) {
            indexingDto.setMarkedForDelete(true);
        }

        return indexingDto;
    }

    private List<DocumentIndexingMetadataDTO> getMultiValueIndexingDTO(
            MetadataDTO metadataDto, List<Object> metadataValues) {

        List<DocumentIndexingMetadataDTO> indexingDtos = new ArrayList<>();
        String metadataId = metadataDto.getId();
        String metadataName = metadataDto.getName();

        // Remove the current selections
        for (DocumentIndexingMetadataDTO dto : currentIndexingDtos.get(metadataDto.getId())) {
            DocumentIndexingMetadataDTO indexingDto =
                    new DocumentIndexingMetadataDTO(
                            dto.getId(), dto.getMetadataId(), metadataName, dto.getValue());
            indexingDto.setMarkedForDelete(true);
            indexingDtos.add(indexingDto);
        }

        // Add new selections
        for (Object valueObj : metadataValues) {
            Object metadataValue = getTypedMetadataValue(valueObj, metadataDto);
            DocumentIndexingMetadataDTO indexingDto =
                    new DocumentIndexingMetadataDTO(null, metadataId, metadataName, metadataValue);
            indexingDtos.add(indexingDto);
        }

        return indexingDtos;
    }

    /**
    * Validates that all required metadata defined in document type exists and has value in indexing
    * data. Used for document creates.
    */
    public void validateAllRequiredFieldsHaveValue() {
        for (MetadataDTO dto : this.metadataMap.values()) {
            String metadataName = dto.getName();
            MetadataDTO metadata = metadataMap.get(metadataName);
            boolean metadataRequired = metadata != null && metadata.isRequired();

            if (metadataRequired) {
                Optional<DocumentIndexingMetadataDTO> field =
                        this.indexingDtos.stream()
                                .filter(f -> metadataName.equals(f.getMetadataName()))
                                .filter(f -> !f.isMarkedForDelete())
                                .findFirst();

                if (!field.isPresent() || isNullOrEmpty(field.get().getValue())) {
                    throwMetadataRequiredException(metadataName);
                }
            }
        }
    }

    /**
    * Validates that entries in indexing data that are marked as required in metadata have values.
    * Used for document updates.
    */
    public void validateRequiredFieldsPresentHaveValue() {
        for (DocumentIndexingMetadataDTO dto : this.indexingDtos) {
            String metadataName = dto.getMetadataName();
            MetadataDTO metadata = metadataMap.get(metadataName);
            boolean metadataRequired = metadata != null && metadata.isRequired();

            if (metadataRequired && !dto.isMarkedForDelete() && isNullOrEmpty(dto.getValue())) {
                throwMetadataRequiredException(metadataName);
            }
        }
    }

    public List<DocumentIndexingMetadataDTO> build() {
        return indexingDtos;
    }

    private boolean isNullOrEmpty(Object value) {
        if (value instanceof String) {
            return Strings.isNullOrEmpty((String) value);
        } else {
            return value == null;
        }
    }

    private void throwMetadataRequiredException(String metadataName) {
        throw new IllegalStateException(
                String.format(
                        "Missing value for required metadata '%s' for document type '%s'.",
                        metadataName, this.documentTypeName));
    }

    private void throwInvalidValuesForSingleSelectMetadata(String metadataName) {
        throw new IllegalStateException(
                String.format(
                        "Multiple values received for single-select field '%s' for document type '%s'.",
                        metadataName, this.documentTypeName));
    }

    private void throwMetadataDoesNotExistException(
            String metadataName, Map<String, MetadataDTO> metadataMap) {
        throw new IllegalStateException(
                String.format(
                        "Document type '%s' is missing metadata object named '%s'. Available metadata: %s.",
                        this.documentTypeName, metadataName, String.join(", ", metadataMap.keySet())));
    }

    private Object getTypedMetadataValue(Object value, MetadataDTO metadata) {
        if (metadata.getDataType() == MetadataTypeEnum.INTEGER
                && value instanceof Integer == false
                && value instanceof Long == false) {

            // Note: This case is validated because if client sends a decimal, DocFinity will silently
            // round to integer and is a potential data loss.
            String message =
                    String.format(
                            "Invalid integer value for metadata object '%s'. Type: %s. Value: %s",
                            metadata.getName(), value.getClass(), value);
            throw new IllegalStateException(message);
        }

        return value;
    }
}
