package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Encapsulates data common to all indexing operations. */
@Data
@RequiredArgsConstructor
public abstract class IndexDocumentArgsBase<T extends IndexDocumentArgsBase<T>> {
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

    /** Returns a self reference. */
    protected abstract T self();

    /**
    * Loads metadata from a map of single values.
    *
    * @param fieldsMap Map of metadata object names with their value to load.
    */
    public T withMetadata(Map<String, Object> fieldsMap) {
        List<DocumentField> fields = new ArrayList<>();

        for (Entry<String, Object> field : fieldsMap.entrySet()) {
            fields.add(new DocumentField(field.getKey(), Arrays.asList(field.getValue())));
        }

        return withMetadata(fields);
    }

    /**
    * Loads metadata from a list.
    *
    * @param fields List of field objects with names and values to load.
    */
    public T withMetadata(List<DocumentField> fields) {
        for (DocumentField field : fields) {
            if (metadata.containsKey(field.getName())) {
                // TODO: Add test
                this.throwDuplicateFieldException(field.getName());
            }

            // TODO: Check for special cases: null, [], [null], [""]

            metadata.putAll(field.getName(), field.getValues());
        }

        return self();
    }

    /** Checks the values of all properties are valid. */
    public void validate() {
        Preconditions.checkNotNull(categoryName, "categoryName is required.");
        Preconditions.checkNotNull(documentTypeName, "documentTypeName is required.");
        Preconditions.checkNotNull(metadata, "metadata is required.");
    }

    private void throwDuplicateFieldException(String fieldName) {
        throw new IllegalStateException(
                String.format("Duplicate field '%s' in indexing metadata values.", fieldName));
    }
}
