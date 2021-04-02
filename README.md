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

## Setup code formatting

This project uses the [Gradle Spotless Plugin](https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless) to enforce the [Google Java Style Guide](https://google.github.io/styleguide/javaguide) (with the addition that it sets indendation to 4 spaces instead of 2). 

Note that if code fails the style guide it will fail the build with a description of the errors. You can also fix all the errors locally by running `./gradlew spotlessApply`.

Setup your IDE to use the formatter:

- Set it to run `./gradlew spotlessDiagnose` to show invalid formatting.
- Set it to run `./gradlew spotlessApply` on save to automatically fix formatting.
- If you use VSCode, install the [Spotless Gradle Extension](https://marketplace.visualstudio.com/items?itemName=richardwillis.vscode-spotless-gradle) that can automatically apply these settings.

## Run CLI with Gradle

You can also use the CLI with arguments from gradle (instead of running from the .jar file) by using the gradle task `run --args="<ARGS>"`. For example, to get documentation of command line options run:

```
./gradlew run --args="--help"
```

## Debug against a live server

TODO: Add documentation of how to quickly debug library against live responses.

# Integration Tests

TODO: To be filled up when the project includes integration tests.