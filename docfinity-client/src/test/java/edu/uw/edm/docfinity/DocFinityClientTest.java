package edu.uw.edm.docfinity;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import edu.uw.edm.docfinity.models.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

        // By default, document has no indexing data.
        setupDocumentIndexingDataReturn();

        // Return the same indexingDto that was passed in.
        when(mockService.reindexDocuments(any())).thenAnswer(i -> Arrays.asList(i.getArguments()[0]));
        when(mockService.indexDocuments(any())).thenAnswer(i -> Arrays.asList(i.getArguments()[0]));
    }

    private void setupDocumentMetadataReturn(MetadataDTO... expectedMetadata) throws Exception {
        when(mockService.getDocumentMetadata(anyString(), anyString()))
                .thenReturn(Arrays.asList(expectedMetadata));
    }

    private void setupDocumentIndexingDataReturn(DocumentIndexingMetadataDTO... indexingDtos)
            throws Exception {
        DocumentIndexingDTO dto = new DocumentIndexingDTO();
        dto.setIndexingMetadata(Arrays.asList(indexingDtos));
        when(mockService.getDocumentIndexingData(anyString())).thenReturn(dto);
    }

    private void setupRunDatasourcesReturn(ExecuteDatasourceResponseDTO... responses)
            throws Exception {
        when(mockService.executeDatasource(any())).thenReturn(Arrays.asList(responses));
    }

    private IndexDocumentArgs buildUpdateArgs(String fieldName, Object fieldValue) {
        DocumentField field = DocumentField.fromSingleValue(fieldName, fieldValue);
        return new IndexDocumentArgs(testDocumentId)
                .withDocumentType("category", "documentType")
                .withMetadata(Arrays.asList(field));
    }

    private IndexDocumentArgs buildUpdateArgs(DocumentField... fields) {
        return new IndexDocumentArgs(testDocumentId)
                .withDocumentType("category", "documentType")
                .withMetadata(Arrays.asList(fields));
    }

    private FileIndexDocumentArgs buildCreateArgs(String fieldName, Object fieldValue) {
        DocumentField field = DocumentField.fromSingleValue(fieldName, fieldValue);
        return new FileIndexDocumentArgs()
                .withDocumentType("category", "documentType")
                .withFile(testFile)
                .withMetadata(Arrays.asList(field));
    }

    private FileIndexDocumentArgs buildCreateArgs(DocumentField... fields) {
        return new FileIndexDocumentArgs()
                .withDocumentType("category", "documentType")
                .withFile(testFile)
                .withMetadata(Arrays.asList(fields));
    }

    @Test
    public void onCreate_shouldIndexDocumentWithMultiSelectMetadata() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field");
        field.setAllowMultipleValues(true);
        setupDocumentMetadataReturn(field);

        // act
        FileIndexDocumentArgs args =
                new FileIndexDocumentArgs()
                        .withDocumentType("category", "documentType")
                        .withFile(testFile)
                        .withMetadata(
                                Arrays.asList(new DocumentField("Field", Arrays.asList("Value1", "Value2"))));

        IndexDocumentResult result = client.uploadIndexAndCommitDocument(args);
        DocumentIndexingDTO indexingDto = result.getIndexingDto();

        // assert
        assertEquals(2, indexingDto.getIndexingMetadata().size());
        assertEquals("Value1", indexingDto.getIndexingMetadata().get(0).getValue());
        assertEquals("Value2", indexingDto.getIndexingMetadata().get(1).getValue());

        assertEquals(1, result.getMetadata().size());
        assertEquals("Field", result.getMetadata().get(0).getName());
        assertThat(result.getMetadata().get(0).getValues(), is(Arrays.asList("Value1", "Value2")));
    }

    @Test
    public void onCreate_shouldThrowErrorIfRequiredMultiSelectMetadataValueIsNull() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setRequired(true);
        field.setAllowMultipleValues(true);
        setupDocumentMetadataReturn(field);

        // act
        FileIndexDocumentArgs args = buildCreateArgs(new DocumentField("Field1", null));
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field1' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void onCreate_shouldThrowErrorIfRequiredMetadataValueIsNull() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setRequired(true);
        setupDocumentMetadataReturn(field);

        // act
        FileIndexDocumentArgs args = buildCreateArgs(new DocumentField("Field1", null));
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field1' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void onCreate_shouldThrowErrorIfRequiredMetadataValueIsEmptyString() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setRequired(true);
        setupDocumentMetadataReturn(field);

        // act
        FileIndexDocumentArgs args = buildCreateArgs("Field1", "");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field1' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void onCreate_shouldThrowErrorIfRequiredMetadataIsMissing() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field1 = new MetadataDTO("111", "Field1");
        MetadataDTO field2 = new MetadataDTO("222", "Field2");
        field2.setRequired(true);
        setupDocumentMetadataReturn(field1, field2);

        // act
        FileIndexDocumentArgs args = buildCreateArgs("Field1", "User Value");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field2' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void onCreate_shouldDeleteDocumentIfErrorIsThrownAfterUpload() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        when(mockService.getDocumentMetadata(anyString(), anyString()))
                .thenThrow(new IOException("Test Error"));

        // act
        FileIndexDocumentArgs args = buildCreateArgs("Field1", "Value1");
        assertThrows(Exception.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        verify(mockService).deleteDocuments(testDocumentId);
    }

    @Test
    public void onUpdate_shouldReplaceMultiSelectMetadata() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("FieldId", "Field");
        field.setAllowMultipleValues(true);

        setupDocumentMetadataReturn(field);
        setupDocumentIndexingDataReturn(
                new DocumentIndexingMetadataDTO("111", "FieldId", "Field", "FieldValue1"),
                new DocumentIndexingMetadataDTO("222", "FieldId", "Field", "FieldValue2"));

        // act
        IndexDocumentArgs args = buildUpdateArgs("Field", "NewValue");
        DocumentIndexingDTO result = client.reindexDocument(args).getIndexingDto();

        // assert
        assertEquals(3, result.getIndexingMetadata().size());
        DocumentIndexingMetadataDTO dto1 = result.getIndexingMetadata().get(0);
        DocumentIndexingMetadataDTO dto2 = result.getIndexingMetadata().get(1);
        DocumentIndexingMetadataDTO dto3 = result.getIndexingMetadata().get(2);
        assertTrue("Expected dto1 to be deleted", dto1.isMarkedForDelete());
        assertTrue("Expected dto2 to be deleted", dto2.isMarkedForDelete());
        assertFalse("Expected dto3 to be added", dto3.isMarkedForDelete());

        assertEquals("111", dto1.getId());
        assertEquals("222", dto2.getId());
        assertNull("Expected dto3 to have null id", dto3.getId());
    }

    /**
    * Note that in this case to be able to validate multi-select required fields, a null value is
    * left on the field.
    */
    @Test
    public void onUpdate_shouldMarkMultiSelectMetadataForDelete() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("FieldId", "Field");
        field.setAllowMultipleValues(true);

        setupDocumentMetadataReturn(field);
        setupDocumentIndexingDataReturn(
                new DocumentIndexingMetadataDTO("111", "FieldId", "Field", "FieldValue1"),
                new DocumentIndexingMetadataDTO("222", "FieldId", "Field", "FieldValue2"));

        // act
        IndexDocumentArgs args = buildUpdateArgs(new DocumentField("Field", null));
        DocumentIndexingDTO result = client.reindexDocument(args).getIndexingDto();

        // assert
        assertEquals(3, result.getIndexingMetadata().size());
        DocumentIndexingMetadataDTO dto1 = result.getIndexingMetadata().get(0);
        DocumentIndexingMetadataDTO dto2 = result.getIndexingMetadata().get(1);
        DocumentIndexingMetadataDTO dto3 = result.getIndexingMetadata().get(2);
        assertTrue("Expected dto1 to be deleted", dto1.isMarkedForDelete());
        assertTrue("Expected dto2 to be deleted", dto2.isMarkedForDelete());
        assertFalse("Expected dto3 to be added", dto3.isMarkedForDelete());

        assertEquals("111", dto1.getId());
        assertEquals("222", dto2.getId());
        assertNull("Expected dto3 to have null id", dto3.getId());
        assertNull("Expected dto3 to have null value", dto3.getValue());
    }

    /**
    * Setup: Document type with two metadata objects, one has a data source that depends on the
    * other.
    */
    @Test
    public void onUpdate_shouldExecuteDatasourceForDependantField() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO parentField = new MetadataDTO("111", "Parent Field");
        parentField.setResponsibilityMapping(Arrays.asList("Child Field"));

        MetadataDTO childField = new MetadataDTO("222", "Child Field");
        DatasourceArgumentPromptDTO prompt = new DatasourceArgumentPromptDTO("Parent Field");
        childField.setDatasourcePrompts(Arrays.asList(prompt));

        setupDocumentMetadataReturn(parentField, childField);
        setupRunDatasourcesReturn(new ExecuteDatasourceResponseDTO("DataSource Value"));

        // act
        IndexDocumentArgs args = buildUpdateArgs("Parent Field", "User Value");
        DocumentIndexingDTO result = client.reindexDocument(args).getIndexingDto();

        // assert
        assertNotNull(result);
        assertEquals(2, result.getIndexingMetadata().size());
        assertEquals("User Value", result.getIndexingMetadata().get(0).getValue());
        assertEquals("DataSource Value", result.getIndexingMetadata().get(1).getValue());
    }

    @Test
    public void onUpdate_shouldMarkFieldForDeleteIfValueSetToNull() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field");
        DocumentIndexingMetadataDTO indexingDto =
                new DocumentIndexingMetadataDTO("testId", "111", "Field");

        setupDocumentMetadataReturn(field);
        setupDocumentIndexingDataReturn(indexingDto);

        // act
        IndexDocumentArgs args = buildUpdateArgs(new DocumentField("Field", null));
        DocumentIndexingDTO result = client.reindexDocument(args).getIndexingDto();

        // assert
        assertNotNull(result);
        assertEquals(1, result.getIndexingMetadata().size());
        assertEquals("testId", result.getIndexingMetadata().get(0).getId());
        assertTrue(result.getIndexingMetadata().get(0).isMarkedForDelete());
    }

    @Test
    public void onUpdate_shouldMarkFieldForDeleteIfValueSetToEmtpy() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field");
        DocumentIndexingMetadataDTO indexingDto =
                new DocumentIndexingMetadataDTO("testId", "111", "Field");

        setupDocumentMetadataReturn(field);
        setupDocumentIndexingDataReturn(indexingDto);

        // act
        IndexDocumentArgs args = buildUpdateArgs("Field", "");
        DocumentIndexingDTO result = client.reindexDocument(args).getIndexingDto();

        // assert
        assertNotNull(result);
        assertEquals(1, result.getIndexingMetadata().size());
        assertEquals("testId", result.getIndexingMetadata().get(0).getId());
        assertTrue(result.getIndexingMetadata().get(0).isMarkedForDelete());
    }

    @Test
    public void onUpdate_shouldThrowErrorIfRequiredMultiSelectMetadataValueIsNull() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setRequired(true);
        field.setAllowMultipleValues(true);
        setupDocumentMetadataReturn(field);

        // act
        IndexDocumentArgs args = buildUpdateArgs(new DocumentField("Field1", null));
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field1' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void onUpdate_shouldThrowErrorIfRequiredMetadataValueIsNull() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setRequired(true);
        setupDocumentMetadataReturn(field);

        // act
        IndexDocumentArgs args = buildUpdateArgs(new DocumentField("Field1", null));
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field1' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void onUpdate_shouldThrowErrorIfRequiredMetadataValueIsEmptyString() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setRequired(true);
        setupDocumentMetadataReturn(field);

        // act
        IndexDocumentArgs args = buildUpdateArgs("Field1", "");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertEquals(
                "Missing value for required metadata 'Field1' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void shouldNotExecuteDatasourceWhenClientProvidesValue() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO parentField = new MetadataDTO("111", "Parent Field");
        parentField.setResponsibilityMapping(Arrays.asList("Child Field"));

        MetadataDTO childField = new MetadataDTO("222", "Child Field");
        DatasourceArgumentPromptDTO prompt = new DatasourceArgumentPromptDTO("Parent Field");
        childField.setDatasourcePrompts(Arrays.asList(prompt));

        setupDocumentMetadataReturn(parentField, childField);
        setupRunDatasourcesReturn(new ExecuteDatasourceResponseDTO("DataSource Value"));

        // act
        IndexDocumentArgs args =
                new IndexDocumentArgs(testDocumentId)
                        .withDocumentType("category", "documentType")
                        .withMetadata(
                                ImmutableMap.of("Parent Field", "Parent Value", "Child Field", "Child Value"));
        DocumentIndexingDTO result = client.reindexDocument(args).getIndexingDto();

        // assert
        List<Object> values =
                result.getIndexingMetadata().stream().map(m -> m.getValue()).collect(Collectors.toList());
        assertEquals(2, values.size());
        assertThat(values, hasItems("Parent Value", "Child Value"));
    }

    @Test
    public void shouldThrowErrorIfDuplicateMetadataEntriesFromClient() throws Exception {
        // arrange
        DocumentField field1 = DocumentField.fromSingleValue("Field", "1");
        DocumentField field2 = DocumentField.fromSingleValue("Field", "2");
        IndexDocumentArgs args =
                new IndexDocumentArgs(testDocumentId).withDocumentType("category", "documentType");

        // act
        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class, () -> args.withMetadata(Arrays.asList(field1, field2)));

        // assert
        assertThat(
                thrown.getMessage(),
                containsString("Duplicate field 'Field' in indexing metadata values."));
    }

    @Test
    public void shouldThrowErrorIfMetadataValueIsInvalidInteger() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO field = new MetadataDTO("111", "Field1");
        field.setDataType(MetadataTypeEnum.INTEGER);
        setupDocumentMetadataReturn(field);

        // act
        IndexDocumentArgs args = buildUpdateArgs("Field1", 100.10);
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertThat(
                thrown.getMessage(), containsString("Invalid integer value for metadata object 'Field1'"));
    }

    @Test
    public void shouldThrowErrorIfSingleSelectMetadataReceivesMultipleValues() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        setupDocumentMetadataReturn(new MetadataDTO("111", "Field"));

        // act
        FileIndexDocumentArgs args =
                new FileIndexDocumentArgs()
                        .withDocumentType("category", "documentType")
                        .withFile(testFile)
                        .withMetadata(
                                Arrays.asList(new DocumentField("Field", Arrays.asList("Value1", "Value2"))));

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Multiple values received for single-select field 'Field' for document type 'documentType'.",
                thrown.getMessage());
    }

    @Test
    public void shouldThrowErrorIfDocumentTypeDoesNotExist() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        when(mockService.getDocumentTypes(any(), any())).thenReturn(new DocumentTypeDTOSearchResult());

        // act
        FileIndexDocumentArgs args = buildCreateArgs("Field1", "Value1");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Document type with category 'category' and name 'documentType' does not exist in server.",
                thrown.getMessage());
    }

    @Test
    public void shouldThrowErrorIfMultipleDocumentTypesMatchFilter() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        when(mockService.getDocumentTypes(any(), any()))
                .thenReturn(DocumentTypeDTOSearchResult.from("DocTypeId-1", "DocTypeId-2"));

        // act
        FileIndexDocumentArgs args = buildCreateArgs("Field1", "Value1");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.uploadIndexAndCommitDocument(args));

        // assert
        assertEquals(
                "Multiple document types with category 'category' and name 'documentType' found in server.",
                thrown.getMessage());
    }

    @Test
    public void shouldThrowErrorIfMetadataDoesNotExist() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        setupDocumentMetadataReturn(new MetadataDTO("111", "Field1"));

        // act
        IndexDocumentArgs args = buildUpdateArgs("Field2", "User Value");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertThat(
                thrown.getMessage(),
                containsString("Document type 'documentType' is missing metadata object named 'Field2'."));
    }

    @Test
    public void shouldThrowErrorIfDatasourcePromptIsMultiSelectField() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO parentField = new MetadataDTO("111", "Parent Field");
        parentField.setResponsibilityMapping(Arrays.asList("Child Field"));
        parentField.setAllowMultipleValues(true);

        MetadataDTO childField = new MetadataDTO("222", "Child Field");
        DatasourceArgumentPromptDTO prompt = new DatasourceArgumentPromptDTO("Parent Field");
        childField.setDatasourcePrompts(Arrays.asList(prompt));

        setupDocumentMetadataReturn(parentField, childField);

        // act
        IndexDocumentArgs args = buildUpdateArgs("Parent Field", "User Value");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertThat(
                thrown.getMessage(),
                containsString("Multi-select fields for datasource prompts are not supported."));
    }

    /**
    * Setup: Field with a datasource that depends on 2 fields but client only sends one of the
    * dependant fields in payload. For the time being this is unsupported.
    */
    @Test
    public void shouldThrowErrorIfDatasourcePromptIsNotIncludedInClientFields() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO parentField1 = new MetadataDTO("111", "First Parent Field");
        parentField1.setResponsibilityMapping(Arrays.asList("Child Field"));

        MetadataDTO parentField2 = new MetadataDTO("222", "Second Parent Field");
        parentField2.setResponsibilityMapping(Arrays.asList("Child Field"));

        MetadataDTO childField = new MetadataDTO("333", "Child Field");
        DatasourceArgumentPromptDTO prompt1 = new DatasourceArgumentPromptDTO("First Parent Field");
        DatasourceArgumentPromptDTO prompt2 = new DatasourceArgumentPromptDTO("Second Parent Field");
        childField.setDatasourcePrompts(Arrays.asList(prompt1, prompt2));

        setupDocumentMetadataReturn(parentField1, parentField2, childField);

        // act
        IndexDocumentArgs args = buildUpdateArgs("First Parent Field", "User Value");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertThat(
                thrown.getMessage(),
                containsString(
                        "Datasource prompt 'Second Parent Field' for field 'Child Field' in document type 'documentType' is missing in client metadata."));
    }

    @Test
    public void shouldThrowErrorIfDatasourceReturnsMoreThanOneValue() throws Exception {
        // arrange
        DocFinityClient client = new DocFinityClient(mockService);
        MetadataDTO parentField = new MetadataDTO("111", "Parent Field");
        parentField.setResponsibilityMapping(Arrays.asList("Child Field"));

        MetadataDTO childField = new MetadataDTO("222", "Child Field");
        DatasourceArgumentPromptDTO prompt1 = new DatasourceArgumentPromptDTO("Parent Field");
        childField.setDatasourcePrompts(Arrays.asList(prompt1));

        setupDocumentMetadataReturn(parentField, childField);
        setupRunDatasourcesReturn(
                new ExecuteDatasourceResponseDTO("DataSource Value1"),
                new ExecuteDatasourceResponseDTO("DataSource Value2"));

        // act
        IndexDocumentArgs args = buildUpdateArgs("Parent Field", "User Value");
        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> client.reindexDocument(args));

        // assert
        assertThat(
                thrown.getMessage(),
                containsString(
                        "Returning lists from datasources is not supported. Field 'Child Field' in document type 'documentType'."));
    }
}
