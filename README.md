# JAWS-REST
Boilerplate Gradle project for developing serverless RESTful API's using Java and Amazon Web Services.
- [Jackson](https://github.com/FasterXML/jackson) for JSON conversion.
- [Guice](https://github.com/google/guice) for dependency injection.
- [Rex](https://github.com/duncpro/Rex) for REST endpoints.
- [JRoute](https://github.com/duncpro/JRoute) for URL routing.
- AWS CDK for deployment.
- TypeScript/Jest for API integration testing.

## Features
### Guice Integration
The request handler includes Guice support in the form of 3 classes.
- `AWSLambdaRuntime` - This injectable provides a mechanism for registering shutdown hooks
which are invoked when the Lambda function exits. 
- `SingletonCloser` - This binding listener will register a shutdown hook for each bound
`Singleton` which implements `AutoCloseable`.
- `AWSLambdaIntegrationModule`- Guice module which provides all the bindings for the aforementioned classes.  
### Integration Testing in TypeScript via Jest and node-fetch 
A Gradle subproject devoted to integration testing is included, making
writing integration tests for your API dead-simple. Run `gradle :integration-tests:run`
to build, deploy, and test your API.
### Infrastructure as Code via AWS CDK
The entire application is deployable via a single Gradle command thanks to
the AWS Cloud Development Kit CLI.

## Gradle Tasks
- `:integration-test:run`: build, run unit tests, deploy, run integration tests
- `destroy`: remove the application from AWS
