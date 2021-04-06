package edu.uw.edm.docfinity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* Represents a metadata name and value consumed from user. The purpose of exposing this class is to
* make it easier for clients to parse json directly into an array of this type.
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocFinityDocumentField {
    /** The name of the document field to index. */
    private String metadataName;

    /** The value of the document field to index. */
    private Object value;
}
