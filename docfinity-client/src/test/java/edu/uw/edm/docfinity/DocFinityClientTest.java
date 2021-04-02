package edu.uw.edm.docfinity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import edu.uw.edm.docfinity.models.DocumentTypesResponse;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class DocFinityClientTest {
    private static DocFinityService mockService;

    @BeforeClass
    public static void setDependencies() throws Exception {
        mockService = mock(DocFinityService.class);
    }

    @Test
    public void shouldRetrieveDocumentTypeId() throws IOException {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        when(mockService.getDocumentTypes("testCategory", "testDocumentType"))
                .thenReturn(DocumentTypesResponse.from("documentType123"));

        // act
        CreateDocumentResult result =
                client.createDocument(null, "testCategory", "testDocumentType", null);

        // assert
        assertEquals("documentType123", result.getDocumentTypeId());
    }
}
