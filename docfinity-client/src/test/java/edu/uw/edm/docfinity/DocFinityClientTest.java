package edu.uw.edm.docfinity;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class DocFinityClientTest {
    private static final String testDocumentTypeId = "documentType123";
    private static final String testDocumentId = "document123";
    private static final URL resource =
            DocFinityClientTest.class.getClassLoader().getResource("test-file.txt");

    private File testFile;
    private DocFinityService mockService;

    @Before
    public void setupDependencies() throws Exception {
        testFile = new File(resource.toURI());
        mockService = mock(DocFinityService.class);

        when(mockService.uploadDocument(any())).thenReturn(testDocumentId);
        when(mockService.getDocumentTypes(any(), any()))
                .thenReturn(DocumentTypeDTOSearchResult.from(testDocumentTypeId));
    }

    private void setupDocumentTypeMetadataReturn(DocumentTypeMetadataDTO... expectedMetadata)
            throws Exception {
        when(mockService.getDocumentTypeMetadata(testDocumentTypeId))
                .thenReturn(Arrays.asList(expectedMetadata));
    }

    private void setupRunDatasourcesReturn(DocumentServerMetadataDTO... indexMetadata)
            throws Exception {
        when(mockService.runDatasources(any())).thenReturn(Arrays.asList(indexMetadata));
    }

    private void verifyRunDatasourcesArg(DocumentIndexingMetadataDTO... expectedMetadata)
            throws Exception {
        DatasourceRunningDTO expectedDatasourceDTO =
                new DatasourceRunningDTO(
                        testDocumentTypeId, testDocumentId, Arrays.asList(expectedMetadata));

        verify(mockService).runDatasources(expectedDatasourceDTO);
    }

    private void verifyIndexDocumentsArg(DocumentIndexingMetadataDTO... documentIndexingMetadata)
            throws Exception {
        DocumentIndexingDTO expected =
                new DocumentIndexingDTO(
                        testDocumentTypeId, testDocumentId, Arrays.asList(documentIndexingMetadata));
        verify(mockService).indexDocuments(new DocumentIndexingDTO[] {expected});
    }

    private void verifyReindexDocumentsArg(DocumentIndexingMetadataDTO... documentIndexingMetadata)
            throws Exception {
        DocumentIndexingDTO expected =
                new DocumentIndexingDTO(
                        testDocumentTypeId, testDocumentId, Arrays.asList(documentIndexingMetadata));
        verify(mockService).reindexDocuments(new DocumentIndexingDTO[] {expected});
    }

    /**
    * Setup: Document type with a single metadata object with a data source set that will change the
    * value when indexing.
    */
    @Test
    public void shouldCreateDocumentWithOneField() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        setupDocumentTypeMetadataReturn(new DocumentTypeMetadataDTO("123", "Test Field"));
        setupRunDatasourcesReturn(
                new DocumentServerMetadataDTO("123", "Test Field", "DataSource Value"));

        // act
        CreateDocumentArgs args =
                new CreateDocumentArgs("testCategory", "testDocumentType")
                        .withFile(testFile)
                        .withMetadata(ImmutableMap.of("Test Field", "User Value"));
        CreateDocumentResult result = client.createDocument(args);

        // assert
        assertEquals(testDocumentId, result.getDocumentId());
        verifyRunDatasourcesArg(new DocumentIndexingMetadataDTO("123", "Test Field", "User Value"));
        verifyIndexDocumentsArg(
                new DocumentIndexingMetadataDTO("123", "Test Field", "DataSource Value"));
    }

    /**
    * Setup: Document type with a single metadata object with a data source set that will change the
    * value when indexing.
    */
    @Test
    public void shouldUpdateDocumentWithOneField() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        setupDocumentTypeMetadataReturn(new DocumentTypeMetadataDTO("123", "Test Field"));
        setupRunDatasourcesReturn(
                new DocumentServerMetadataDTO("123", "Test Field", "DataSource Value"));

        // act
        UpdateDocumentArgs args =
                new UpdateDocumentArgs(testDocumentId, "testCategory", "testDocumentType")
                        .withMetadata(ImmutableMap.of("Test Field", "User Value"));
        UpdateDocumentResult result = client.updateDocument(args);

        // assert
        assertNotNull(result);
        verifyRunDatasourcesArg(new DocumentIndexingMetadataDTO("123", "Test Field", "User Value"));
        verifyReindexDocumentsArg(
                new DocumentIndexingMetadataDTO("123", "Test Field", "DataSource Value"));
    }

    @Test
    public void shouldThrowErrorIfDocumentTypeDoesNotExist() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        when(mockService.getDocumentTypes(any(), any())).thenReturn(new DocumentTypeDTOSearchResult());

        // act
        CreateDocumentArgs args =
                new CreateDocumentArgs("testCategory", "testDocumentType").withFile(testFile);

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.createDocument(args));

        // assert
        assertEquals(
                "Document type with category 'testCategory' and name 'testDocumentType' does not exist in server.",
                thrown.getMessage());
    }

    @Test
    public void shouldThrowErrorIfMultipleDocumentTypesMatchFilter() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        when(mockService.getDocumentTypes(any(), any()))
                .thenReturn(DocumentTypeDTOSearchResult.from("DocTypeId-1", "DocTypeId-2"));

        // act
        CreateDocumentArgs args =
                new CreateDocumentArgs("testCategory", "testDocumentType").withFile(testFile);
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.createDocument(args));

        // assert
        assertEquals(
                "Multiple document types with category 'testCategory' and name 'testDocumentType' found in server.",
                thrown.getMessage());
    }

    @Test
    public void shouldNotUploadDocumentIfUserMetadataValidationFails() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        setupDocumentTypeMetadataReturn(new DocumentTypeMetadataDTO("111", "Field1", true));

        // act
        CreateDocumentArgs args =
                new CreateDocumentArgs("category", "documentType")
                        .withFile(testFile)
                        .withMetadata(ImmutableMap.of("Field2", "User Value"));
        assertThrows(IllegalStateException.class, () -> client.createDocument(args));

        // assert
        verify(mockService, never()).uploadDocument(any());
    }

    @Test
    public void shouldDeleteDocumentIfErrorIsThrownAfterUpload() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        setupDocumentTypeMetadataReturn(new DocumentTypeMetadataDTO("111", "Field1"));
        when(mockService.runDatasources(any())).thenThrow(new IOException("Test Error"));

        // act
        CreateDocumentArgs args =
                new CreateDocumentArgs("category", "documentType")
                        .withFile(testFile)
                        .withMetadata(ImmutableMap.of("Field1", "User Value"));
        assertThrows(Exception.class, () -> client.createDocument(args));

        // assert
        verify(mockService).deleteDocuments(testDocumentId);
    }
}
