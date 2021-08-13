# JAWS-REST
Boilerplate Gradle project for developing serverless RESTful API's using Java and Amazon Web Services.

## Packages
### aws-cloud-app
Amazon CDK application encapsulating the API Gateway REST API and Lambda handler function.
### integration-tests
A brief test suite verifying the deployed CDK application can respond to HTTP requests.
### request-handler-lambda
The Lambda function which handles to API Gateway Proxy Request events. This is a catch-all
handler which responds to all requests regardless of path and HTTP method.


## Motivation
I find AWS's official documentation to be helpful but not ideal for getting started quickly. This repository
aims to help Java developers like myself get started developing serverless applications more quickly than what
is traditionally possible using the AWS documentation.

## Features
### Guice Integration
The request handler includes Guice support in the form of 3 classes.
- `AWSLambdaIntegrationModule`- Guice module which provides all the bindings.
- `AWSLambdaRuntime` - This injectable provides a mechanism for registering shutdown hooks
which are invoked when the Lambda function exits. 
- `SingletonCloser` - This binding listener will register a shutdown hook for each bound
`Singleton` which implements `AutoCloseable`.
### Integration Testing in TypeScript via Jest and node-fetch 
A Gradle subproject devoted to integration testing is included making
writing integration tests for your API dead-simple. Run `gradle :integration-tests:run`
to build, deploy, and test your API.
### Infrastructure as Code via AWS CDK
The entire application is deployable via a single Gradle command thanks to
the AWS Cloud Development Kit CLI.

## Dependencies
- AWS CDK CLI
- Node.js/NPM
- Java 11
