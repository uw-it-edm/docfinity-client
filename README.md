# DocFinity Client

Java client library to create and update documents in DocFinity.

![CI badge](https://github.com/uw-it-edm/docfinity-client/actions/workflows/ci.yml/badge.svg?branch=develop)

## Motivation

Abstract the complexity of creating and updating documents in DocFinity with the goal of re-using the core logic in a 
REST API if it becomes necessary. For more information see [EDM DocFinity Service Integration Proposal](https://wiki.cac.washington.edu/x/rcT8Bw).

## Design Principles

- Abide by DocFinity resource representation.
- Do NOT expose a class abstraction to public surface.
- Main logic should be 100% re-usable if we decide to build a REST API.
- All errors (validation, request, etc) should be transmitted by exceptions and logging.

## Setup for Development

### Build and Test

```
./gradlew build
```

## CI and Publishing

Release builds published to [UW Artifactory](https://artifact.s.uw.edu/artifactory/webapp/#/artifacts/browse/tree/General/eaiw-release-local/edu/uw/edm/docfinity/docfinity-client) via GitHub Actions.