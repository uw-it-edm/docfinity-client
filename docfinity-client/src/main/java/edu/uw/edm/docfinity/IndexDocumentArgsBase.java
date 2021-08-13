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

/** Encapsulates data common to all indexing operations. */
@Data
public abstract class IndexDocumentArgsBase<T extends IndexDocumentArgsBase<T>> {
    /** Category name to index document. */
    private String category;

    /** Document type name to index document. */
    private String documentType;

    /**
    * Map of metadata object names with their value to use when indexing.
    *
    * @apiNote To set multi-select fields, add multiple values to the same key.
    */
    private Multimap<String, Object> metadata = ArrayListMultimap.create();

    /** Returns a self reference. */
    protected abstract T self();

    /** * Sets the category and document type names. */
    public T withDocumentType(String category, String documentType) {
        this.setCategory(category);
        this.setDocumentType(documentType);
        return self();
    }

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
                this.throwDuplicateFieldException(field.getName());
            }

            String name = field.getName();
            List<Object> values = field.getValues();

            if (values == null || values.size() == 0) {
                metadata.put(name, null);
            } else {
                metadata.putAll(field.getName(), field.getValues());
            }
        }

        return self();
    }

    /** Checks the values of all properties are valid. */
    public void validate() {
        Preconditions.checkNotNull(category, "categoryName is required.");
        Preconditions.checkNotNull(documentType, "documentTypeName is required.");

        // TODO: Check for special cases: [null], [""]
        Preconditions.checkNotNull(metadata, "metadata is required.");
    }

    private void throwDuplicateFieldException(String fieldName) {
        throw new IllegalStateException(
                String.format("Duplicate field '%s' in indexing metadata values.", fieldName));
    }
}
