package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.io.IOException;
import java.util.List;
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
    public CreateDocumentResult createDocument(CreateDocumentArgs args) throws IOException {
        Preconditions.checkNotNull(args, "args is required.");
        args.validate();

        // 1. Get the document type id from the category and document names.
        String documentTypeId = getDocumentTypeId(args.getCategoryName(), args.getDocumentTypeName());
        log.info("Retrieved document type id: {}", documentTypeId);

        // 2. Get the metadata objects from the document type id.
        List<DocumentTypeMetadataDTO> metadataDtos = getMetadataDefinitions(documentTypeId);

        // 3. Upload file.
        String documentId = this.service.uploadDocument(args.getFile());
        log.info("File uploaded, document id: {}", documentId);

        // 4. Execute data sources from the partial client metadata and retrieve full server metadata.
        DocFinityDtoMapper dtoMapper =
                new DocFinityDtoMapper(documentTypeId, documentId, args.getMetadata());

        DatasourceRunningDTO datasourceDto = dtoMapper.buildDatasourceDtoFromMetadata(metadataDtos);
        List<DocumentServerMetadataDTO> serverMetadataDtos = this.service.runDatasources(datasourceDto);

        // 5. Index and commit the document using the calculated values from datasources.
        DocumentIndexingDTO indexRequest =
                dtoMapper.buildIndexingDtoFromServerMetadataDtos(serverMetadataDtos);
        this.service.indexDocuments(indexRequest);

        // TODO: 6. Delete file if indexing fails

        // Build result to return to client.
        return new CreateDocumentResult(documentId);
    }

    private List<DocumentTypeMetadataDTO> getMetadataDefinitions(String documentTypeId)
            throws IOException {
        List<DocumentTypeMetadataDTO> metadataDefinitions =
                this.service.getDocumentTypeMetadata(documentTypeId);

        // TODO: Error check and validate reponse.
        return metadataDefinitions;
    }

    private String getDocumentTypeId(String categoryName, String documentTypeName)
            throws IOException {
        DocumentTypeDTOSearchResult documentTypes =
                this.service.getDocumentTypes(categoryName, documentTypeName);

        // TODO: Error check and validate response.
        return documentTypes.getResults().get(0).getId();
    }
}
