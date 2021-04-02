package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Encapsulates the arguments for creating a document. */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CreateDocumentArgs {
    /** File to upload. */
    private final File file;

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
    public CreateDocumentArgs withMetadata(Map<String, Object> metadataMap) {
        CreateDocumentArgs clone = this.newCopy();
        clone.metadata.clear();

        metadataMap.entrySet().stream()
                .forEach(entry -> clone.metadata.put(entry.getKey(), entry.getValue()));
        return clone;
    }

    /**
    * Creates new arguments class with metadata from a list.
    *
    * @param metadataList List of field objects with names and values to load.
    */
    public CreateDocumentArgs withMetadata(List<DocFinityDocumentField> metadataList) {
        CreateDocumentArgs clone = this.newCopy();
        clone.metadata.clear();

        metadataList.stream().forEach(field -> clone.metadata.put(field.getName(), field.getValue()));
        return clone;
    }

    /** TODO: Add comments. */
    public void validate() {
        Preconditions.checkNotNull(categoryName, "categoryName is required.");
        Preconditions.checkNotNull(documentTypeName, "documentTypeName is required.");
        Preconditions.checkNotNull(metadata, "metadata is required.");
        Preconditions.checkArgument(file.exists(), "file must exist.");
    }

    private CreateDocumentArgs newCopy() {
        CreateDocumentArgs clone = new CreateDocumentArgs(file, categoryName, documentTypeName);
        clone.setMetadata(ArrayListMultimap.create(this.metadata));
        return clone;
    }
}
