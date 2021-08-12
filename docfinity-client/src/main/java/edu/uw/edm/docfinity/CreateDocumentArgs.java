package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import java.io.File;
import lombok.Getter;
import lombok.Setter;

/** Encapsulates the arguments for creating a document. */
public class CreateDocumentArgs extends IndexDocumentArgs<CreateDocumentArgs> {
    /** File to upload. */
    @Getter @Setter private File file;

    /** File content to upload. */
    @Getter @Setter private byte[] fileContent;

    /** File name to upload. */
    @Getter @Setter private String fileName;

    public CreateDocumentArgs(String categoryName, String documentTypeName) {
        super(categoryName, documentTypeName);
    }

    /**
    * Creates new arguments class with a File.
    *
    * @param file File to upload.
    */
    public CreateDocumentArgs withFile(File file) {
        this.setFile(file);
        return this;
    }

    /**
    * Creates new arguments class with file to upload as byte array.
    *
    * @param fileContent Content of file to upload.
    * @param fileName Name of file to upload.
    */
    public CreateDocumentArgs withFileContent(byte[] fileContent, String fileName) {
        this.setFileContent(fileContent);
        this.setFileName(fileName);
        return this;
    }

    /** Checks the values of all properties are valid. */
    @Override
    public void validate() {
        super.validate();

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

    @Override
    protected CreateDocumentArgs self() {
        return this;
    }
}
