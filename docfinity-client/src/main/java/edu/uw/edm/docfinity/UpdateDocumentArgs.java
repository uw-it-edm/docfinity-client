package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Encapsulates the arguments for updating a document. */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateDocumentArgs implements IndexDocumentArgs {
    /** Identifier of document to update. */
    private final String documentId;

    /** Category name to index document. */
    private final String categoryName;

    /** Document type name to index document. */
    private final String documentTypeName;

    /**
    * Map of metadata object names with their value to use when indexing.
    *
    * @apiNote To set multi-select fields, add multiple values to the same key.
    */
    private Multimap<String, Object> metadata = ArrayListMultimap.create();

    /**
    * Creates new arguments class with metadata from a dictionary.
    *
    * @param metadataMap Map of metadata object names with their value to load.
    */
    public UpdateDocumentArgs withMetadata(Map<String, Object> metadataMap) {
        UpdateDocumentArgs cloned = this.newCopy();
        cloned.metadata.clear();

        metadataMap.entrySet().stream()
                .forEach(entry -> cloned.metadata.put(entry.getKey(), entry.getValue()));
        return cloned;
    }

    /**
    * Creates new arguments class with metadata from a list.
    *
    * @param metadataList List of field objects with names and values to load.
    */
    public UpdateDocumentArgs withMetadata(List<DocFinityDocumentField> metadataList) {
        UpdateDocumentArgs cloned = this.newCopy();
        cloned.metadata.clear();

        metadataList.stream()
                .forEach(field -> cloned.metadata.put(field.getMetadataName(), field.getValue()));
        return cloned;
    }

    /** Checks the values of all properties are valid. */
    public void validate() {
        Preconditions.checkNotNull(documentId, "documentId is required.");
        Preconditions.checkNotNull(categoryName, "categoryName is required.");
        Preconditions.checkNotNull(documentTypeName, "documentTypeName is required.");
        Preconditions.checkNotNull(metadata, "metadata is required.");
    }

    private UpdateDocumentArgs newCopy() {
        UpdateDocumentArgs cloned = new UpdateDocumentArgs(documentId, categoryName, documentTypeName);
        cloned.setMetadata(ArrayListMultimap.create(this.metadata));
        return cloned;
    }
}
