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
public class CreateDocumentArgs implements IndexDocumentArgs {
    /** File to upload. */
    private File file;

    /** File content to upload. */
    private byte[] fileContent;

    /** File name to upload. */
    private String fileName;

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
        CreateDocumentArgs cloned = this.newCopy();
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
    public CreateDocumentArgs withMetadata(List<DocFinityDocumentField> metadataList) {
        CreateDocumentArgs cloned = this.newCopy();
        cloned.metadata.clear();

        metadataList.stream()
                .forEach(field -> cloned.metadata.put(field.getMetadataName(), field.getValue()));
        return cloned;
    }

    /**
    * Creates new arguments class with a File.
    *
    * @param file File to upload.
    */
    public CreateDocumentArgs withFile(File file) {
        CreateDocumentArgs cloned = this.newCopy();
        cloned.setFile(file);
        return cloned;
    }

    /**
    * Creates new arguments class with file to upload as byte array.
    *
    * @param fileContent Content of file to upload.
    * @param fileName Name of file to upload.
    */
    public CreateDocumentArgs withFileContent(byte[] fileContent, String fileName) {
        CreateDocumentArgs cloned = this.newCopy();
        cloned.setFileContent(fileContent);
        cloned.setFileName(fileName);
        return cloned;
    }

    /** Checks the values of all properties are valid. */
    public void validate() {
        Preconditions.checkNotNull(categoryName, "categoryName is required.");
        Preconditions.checkNotNull(documentTypeName, "documentTypeName is required.");
        Preconditions.checkNotNull(metadata, "metadata is required.");

        if (file == null && fileContent == null) {
            throw new IllegalStateException("file or fileContent must be specified.");
        } else if (file != null && fileContent != null) {
            throw new IllegalStateException("Cannot specify both file and fileContent.");
        } else if (file != null) {
            Preconditions.checkArgument(file.exists(), "file must exist.");
        } else {
            Preconditions.checkNotNull(fileName, "fileName is required if fileContent is specified.");
        }
    }

    private CreateDocumentArgs newCopy() {
        CreateDocumentArgs cloned = new CreateDocumentArgs(categoryName, documentTypeName);
        cloned.setFile(file);
        cloned.setFileContent(fileContent);
        cloned.setFileName(fileName);
        cloned.setMetadata(ArrayListMultimap.create(this.metadata));
        return cloned;
    }
}
