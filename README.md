# DocFinity Client

Java client library to create and update documents in DocFinity.

Table of Contents:

1. [Install](#Install)
1. [Use](#Use)
1. [Design](#Design)
1. [Setup for Development](#Setup-for-Development)

# Install

Release builds published via GitHub Actions to UW Artifactory [here](https://artifact.s.uw.edu/artifactory/webapp/#/artifacts/browse/tree/General/eaiw-release-local/edu/uw/edm/docfinity/docfinity-client).

![CI badge](https://github.com/uw-it-edm/docfinity-client/actions/workflows/ci.yml/badge.svg?branch=develop)

```
dependencies {
    compile("edu.uw.edm.docfinity:docfinity-client:latest")
}
```
# Use

## Basic Create

```java
DocFinityClient client = new DocFinityClient("<DocFinity URL>", "<DocFinity API KEY>");

CreateDocumentArgs createArgs = new CreateDocumentArgs("<Category>", "<DocumentTypeName>")
        .withFile(new File("<path to file>"))
        .withMetadata(ImmutableMap.of("<Metadata Name>", "<Metadata Value>"));

CreateDocumentResult result = client.createDocument(createArgs);
System.out.printf(result.getDocumentId());
```

## Basic Update

```java
DocFinityClient client = new DocFinityClient("<DocFinity URL>", "<DocFinity API KEY>");

UpdateDocumentArgs updateArgs = new UpdateDocumentArgs("<DocumentId>", "<Category>", "<DocumentTypeName>")
        .withMetadata(ImmutableMap.of("<Metadata Name>", "<Metadata Value>"));

UpdateDocumentResult result = client.updateDocument(updateArgs);
```

## Update to Clear a Metadata Field

To clear a metadata field when updating a document, set the field to null or empty string.

```java
UpdateDocumentArgs updateArgs = new UpdateDocumentArgs("<DocumentId>", "<Category>", "<DocumentTypeName>")
        .withMetadata(ImmutableMap.of("<Metadata Name>", null));

UpdateDocumentResult result = client.updateDocument(updateArgs);
```

## Create with different metadata types

```java
CreateDocumentArgs createArgs =  new CreateDocumentArgs("<Category>", "<DocumentTypeName>")
        .withMetadata(ImmutableMap.of(
            "Integer Field", 100,
            "Decimal Field", 100.99
            "Date Field", 1627282800000 // <-- milliseconds elapsed since January 1, 1970.
        ));
```

## Create with multi-select metadata

```java
Multimap<String, Object> metadata = ArrayListMultimap.create();
metadata.put("MultiSelect Field", "First Value");
metadata.put("MultiSelect Field", "Second Value");
metadata.put("MultiSelect Field", "Third Value");
CreateDocumentArgs args = new CreateDocumentArgs("<Category>", "<DocumentTypeName>")
        .withMetadata(metadata));
```

## Create with byte[] as content

```java
byte[] content;
CreateDocumentArgs args =  new CreateDocumentArgs("<Category>", "<DocumentTypeName>")
        .withFileContent(content, "file name.txt"));
```

# Design
## Motivation

This library abstracts the complexity of creating and updating documents in DocFinity with the goal of re-using the core logic in a 
REST API if it becomes necessary. For more information see [EDM DocFinity Service Integration Proposal](https://wiki.cac.washington.edu/x/rcT8Bw).

## Design Principles

- Abide by DocFinity resource representation.
- Do NOT expose a class abstraction to public surface.
- Main logic should be 100% re-usable if we decide to build a REST API.
- All errors (validation, request, etc) should be transmitted by exceptions and logging.

## Explanation of Indexing Steps

1. Get DocumentTypeId
    - Since user is not expected to know the internal identifier for the DocumentType, given a document type name and category name, retrieve the DocumentTypeId.
    - Validate that document type exists.
2. Upload Document
    - Retrieves the DocumentId for the new document.
3. Get Metadata Definitions with Datasource Information
    - Since user is not expected to know the internal identifiers for metadata objects, using the DocumentTypeId and DocumentId use the `/indexing/controls` end-point to retrieve the metadata that includes datasource information.
    - Validate that the metadata names from user exist for the document type.
4. Execute DataSources
    - For each field that has a datasource defined that depends on another field that is being indexed, use the `/indexing/executeDatasource` end-point to run the datasource and retrieve the result.
    - Validate that all required fields have values.
5. Index and Commit
    - Using the response from the previous step, send a request to `/indexing/index/commit` to index and commit the new document.
6. Delete Document on Error
    - If there is an error at any point after uploading the document, delete it.

## Explanation of Re-Indexing Steps

1. Get DocumentTypeId
    - Same as Indexing flow.
2. Get Metadata Definitions with Datasource Information
    - Same as Indexing flow.
3. Get current Indexed data
    - Use the `/indexing/data` end-point to retrieve the current indexed data for the document.
4. Execute DataSources
    - Same as Indexing flow.
5. Re-Index
    - Handle metadata fields that are removed by sending `markedForDelete` property on the appropiate fields.
    - Using the response from the previous steps, send a request to `/indexing/reindex` to send partial updates to patch document metadata.

## IMPORTANT: Limitations and Caveats

1. Datasources only run if ALL dependency fields are provided by client in the indexing payload.
2. If client provides an indexing value for a field that has a datasource, the client value will take precedence over the datasource result.
3. Datasources for fields must return a single value, if an eligible datasource returns a list an error will be thrown.
4. Dependency fields for datasources must be single-select fields.
5. Validation of values between parent-child fields is not supported.

# Setup for Development

## Build and run unit tests

```
./gradlew build
```

## Setup code formatting

This project uses the [Gradle Spotless Plugin](https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless) to enforce the [Google Java Style Guide](https://google.github.io/styleguide/javaguide) (with the addition that it sets indendation to 4 spaces instead of 2). 

Note that if code fails the style guide it will fail the build with a description of the errors. You can also fix all the errors locally by running `./gradlew spotlessApply`.

Setup your IDE to use the formatter:

- Set it to run `./gradlew spotlessDiagnose` to show invalid formatting.
- Set it to run `./gradlew spotlessApply` on save to automatically fix formatting.
- If you use VSCode, install the [Spotless Gradle Extension](https://marketplace.visualstudio.com/items?itemName=richardwillis.vscode-spotless-gradle) that can automatically apply these settings.

## Debug against a live server

Configure your IDE to launch the `docfinity-client-cli/src/main/java/edu/uw/edm/docfinity/cli/DocFinityClientCLI.java` class and setup the following startup arguments:

- -u: DocFinity base url.
- -k: DocFinity API Key.
- -f: Path to file to upload or "test" to use a sample file.
- -c: Category name to index document.
- -d: Document type name to index document.
- -j: Metadata array to index as json string, ie. `"[{ \"metadataName\": \"FieldName\": \"value\": \"FieldValue\" }]"`
- --trace: Turns on request tracing

Note: You can also specify a file to load the metadata json from (use the `-m` parameter).

## Run CLI with Gradle

You can also use the CLI with arguments from gradle (instead of running from the .jar file) by using the gradle task `run --args="<ARGS>"` with the same arguments as described in the previous section. For example, to get documentation of command line options run:

```
./gradlew run --args="--help"
```
