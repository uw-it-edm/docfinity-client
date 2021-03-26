# DocFinity Client

Java client library to create and update documents in DocFinity.

Table of Contents:

1. [Install](#Install)
1. [Use](#Use)
1. [CLI](#CLI)
1. [Design](#Design)
1. [Setup for Development](#Setup-for-Development)
1. [Integration Tests](#Integration-Tests)

# Install

Release builds published via GitHub Actions to UW Artifactory [here](https://artifact.s.uw.edu/artifactory/webapp/#/artifacts/browse/tree/General/eaiw-release-local/edu/uw/edm/docfinity/docfinity-client).

![CI badge](https://github.com/uw-it-edm/docfinity-client/actions/workflows/ci.yml/badge.svg?branch=develop)

```
dependencies {
    compile("edu.uw.edm.docfinity:docfinity-client:latest")
}
```
# Use

TODO: Once library is implemented add sample use case here.

# CLI

To creates/update documents from command line, you can use the cli published in UW Artifactory [here](https://artifact.s.uw.edu/artifactory/webapp/#/artifacts/browse/tree/General/eaiw-release-local/edu/uw/edm/docfinity/docfinity-client-cli/).

Samples:

```
TODO: Write sample of how to use the CLI.
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

# Setup for Development

## Build and run unit tests

```
./gradlew build
```

## Run test against a live server

### - with JUnit

The simplest way to run and debug the client library againt a live DocFinity server is to use the `docfinity-client-cli/src/test/java/edu/uw/edm/docfinity/cli/DocFinityClientCLITest.java` unit test. This can be executed within your IDE.

TODO: Add documentation of how to set up DocFinity url and API key for the unit test.

### - with Gradle

You can also use the CLI with arguments from gradle (instead of running from the .jar file):

```
TODO: Document usage with arguments once the CLI is done.

./gradlew run --args="<cli-args-here>"
```

# Integration Tests

TODO: To be filled up when the project includes integration tests.