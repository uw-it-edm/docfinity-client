package edu.uw.edm.docfinity;

import com.google.common.collect.Multimap;

public interface IndexDocumentArgs {
    /** Category name to index document. */
    String getCategoryName();

    /** Document type name to index document. */
    String getDocumentTypeName();

    /** Map of metadata object names with their value to use when indexing. */
    Multimap<String, Object> getMetadata();
}
