package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/** Abstracts the create and update operations for DocFinity documents. */
public class DocFinityClient {
    private final DocFinityService service;

    /**
    * Creates a new instance of the DocFinityClient.
    *
    * @param url The DocFinity base url.
    * @param apiKey The API key to use to interact with DocFinity.
    */
    public DocFinityClient(String url, String apiKey) {
        this.service = new DocFinityServiceImpl(url, apiKey, null);
    }

    /**
    * Creates a new instance of the DocFinityClient.
    *
    * @param url The DocFinity base url.
    * @param apiKey The API key to use to interact with DocFinity.
    * @param auditUser DocFinity account username to use for auditing calls and document history.
    */
    public DocFinityClient(String url, String apiKey, String auditUser) {
        this.service = new DocFinityServiceImpl(url, apiKey, auditUser);
    }

    /**
    * Creates a new instance of the DocFinityClient, intended for testing purposes.
    *
    * @param service An implementation of the DocFinityService.
    */
    public DocFinityClient(DocFinityService service) {
        this.service = service;
    }

    /**
    * Uploads and indexes a document to DocFinity.
    *
    * @param args Class that encapsulates arguments for create document operation.
    */
    public CreateDocumentResult createDocument(CreateDocumentArgs args) throws Exception {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        DocFinityDtoMapper mapper = new DocFinityDtoMapper(args);

        // 1. Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(args.getCategoryName(), args.getDocumentTypeName());
        log.info("Retrieved document type id: {}", documentTypeId);

        // 2. Get the metadata objects from the document type id and validate inputs.
        Map<String, DocumentTypeMetadataDTO> metadata = getDocumentTypeMetadata(documentTypeId);
        List<DocumentIndexingMetadataDTO> partialDtos = mapper.getPartialIndexingDtos(metadata);

        // 3. Upload file.
        String documentId = uploadFile(args);
        log.info("File uploaded, document id: {}", documentId);

        try {
            // 4. Execute data sources from the partial client metadata and retrieve full server metadata.
            List<DocumentServerMetadataDTO> serverDtos =
                    this.service.runDatasources(
                            new DatasourceRunningDTO(documentTypeId, documentId, partialDtos));

            // 5. Index and commit the document using the calculated values from datasources.
            List<DocumentIndexingMetadataDTO> finalDtos =
                    mapper.getFinalIndexingDtos(metadata, serverDtos);
            this.service.indexDocuments(new DocumentIndexingDTO(documentTypeId, documentId, finalDtos));

        } catch (Exception e) {
            // 6. If there is an error after the file has been upload it, need to delete it from server.
            this.tryDeleteDocument(documentId);
            throw e;
        }

        // Build result to return to client.
        return new CreateDocumentResult(documentId);
    }

    /**
    * Reindexes a document to DocFinity.
    *
    * @param args Class that encapsulates arguments for update document operation.
    */
    public UpdateDocumentResult updateDocument(UpdateDocumentArgs args) throws Exception {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        DocFinityDtoMapper mapper = new DocFinityDtoMapper(args);
        String documentId = args.getDocumentId();

        // 1. Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(args.getCategoryName(), args.getDocumentTypeName());
        log.info("Retrieved document type id: {}", documentTypeId);

        // 2. Get the metadata objects from the document type id and validate inputs.
        Map<String, DocumentTypeMetadataDTO> metadata = getDocumentTypeMetadata(documentTypeId);
        List<DocumentIndexingMetadataDTO> partialDtos = mapper.getPartialIndexingDtos(metadata);

        // 3. Execute data sources from the partial client metadata and retrieve full server metadata.
        List<DocumentServerMetadataDTO> serverDtos =
                this.service.runDatasources(
                        new DatasourceRunningDTO(documentTypeId, documentId, partialDtos));

        // 4. Reindex the document using the calculated values from datasources.
        List<DocumentIndexingMetadataDTO> finalDtos = mapper.getFinalIndexingDtos(metadata, serverDtos);
        this.service.reindexDocuments(new DocumentIndexingDTO(documentTypeId, documentId, finalDtos));

        // Build result to return to client.
        return new UpdateDocumentResult();
    }

    private String uploadFile(CreateDocumentArgs args) throws IOException {
        String documentId;
        if (args.getFile() != null) {
            documentId = this.service.uploadDocument(args.getFile());
        } else {
            documentId = this.service.uploadDocument(args.getFileContent(), args.getFileName());
        }
        return documentId;
    }

    private void tryDeleteDocument(String documentId) {
        try {
            this.service.deleteDocuments(documentId);
            log.info("Document deleted due to indexing error, id: {}", documentId);
        } catch (IOException e) {
            log.error("Failed to delete document '%s'. Error Message: ", documentId, e.getMessage());
        }
    }

    private Map<String, DocumentTypeMetadataDTO> getDocumentTypeMetadata(String documentTypeId)
            throws IOException {

        List<DocumentTypeMetadataDTO> metadata = this.service.getDocumentTypeMetadata(documentTypeId);

        Preconditions.checkNotNull(metadata, "getDocumentTypeMetadata() result is null.");

        return metadata.stream()
                .collect(Collectors.toMap(DocumentTypeMetadataDTO::getMetadataName, m -> m));
    }

    private String getDocumentTypeId(String categoryName, String documentTypeName)
            throws IOException {
        DocumentTypeDTOSearchResult documentTypes =
                this.service.getDocumentTypes(categoryName, documentTypeName);

        Preconditions.checkNotNull(documentTypes, "getDocumentTypes() result is null.");
        int count = documentTypes.getTotalAvailable();

        if (count == 1) {
            return documentTypes.getResults().get(0).getId();
        } else if (count > 1) {
            throw new IllegalStateException(
                    String.format(
                            "Multiple document types with category '%s' and name '%s' found in server.",
                            categoryName, documentTypeName));
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Document type with category '%s' and name '%s' does not exist in server.",
                            categoryName, documentTypeName));
        }
    }
}
