package edu.uw.edm.docfinity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import edu.uw.edm.docfinity.models.DatasourceRunningDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingDTO;
import edu.uw.edm.docfinity.models.DocumentIndexingMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentServerMetadataDTO;
import edu.uw.edm.docfinity.models.DocumentTypeDTOSearchResult;
import edu.uw.edm.docfinity.models.DocumentTypeMetadataDTO;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

public class DocFinityClientTest {
    private static DocFinityService mockService;
    private static final String testDocumentTypeId = "documentType123";
    private static final String testDocumentId = "document123";
    private static final URL resource =
            DocFinityClientTest.class.getClassLoader().getResource("test-file.txt");
    private static File testFile;

    @BeforeClass
    public static void setupDependencies() throws Exception {
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
                new CreateDocumentArgs(testFile, "testCategory", "testDocumentType")
                        .withMetadata(ImmutableMap.of("Test Field", "User Value"));
        CreateDocumentResult result = client.createDocument(args);

        // assert
        assertEquals(testDocumentId, result.getDocumentId());
        verifyRunDatasourcesArg(new DocumentIndexingMetadataDTO("123", "User Value"));
        verifyIndexDocumentsArg(new DocumentIndexingMetadataDTO("123", "DataSource Value"));
    }
}
