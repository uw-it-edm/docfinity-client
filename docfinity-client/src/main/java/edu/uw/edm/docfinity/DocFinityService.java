package edu.uw.edm.docfinity;

import edu.uw.edm.docfinity.models.DocumentTypesResponse;
import java.io.IOException;

/** Abstracts the interaction with DocFinity REST API. */
public interface DocFinityService {
    /**
    * Represents call to '/webservices/rest/documentType' to retrieve document types.
    *
    * @param categoryName Category name to use in query filter.
    * @param documentTypeName Document type name to use in query filter.
    * @throws IOException
    */
    DocumentTypesResponse getDocumentTypes(String categoryName, String documentTypeName)
            throws IOException;
}
