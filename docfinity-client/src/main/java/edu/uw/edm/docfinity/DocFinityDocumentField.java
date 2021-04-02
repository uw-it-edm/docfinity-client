package edu.uw.edm.docfinity;

import lombok.Data;

/**
* Represents a metadata name and value consumed from user. The purpose of exposing this class is to
* make it easier for clients to parse json directly into an array of this type.
*/
@Data
public class DocFinityDocumentField {
    /** The name of the document field to index. */
    private String name;

    /** The value of the document field to index. */
    private Object value;
}
