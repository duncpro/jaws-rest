# JAWS-REST
Boilerplate Gradle project for developing serverless RESTful APIs on AWS using Java.

The primary motivation behind JAWS is to provide a platform for developing REST APIs which have minimal hosting cost,
until the user-base grows significantly. By leveraging technologies
such as **RDS Aurora Serverless**, **AWS API Gateway**, and **AWS Lambda**, JAWS applications have a hosting cost
that is proportional to the amount of API calls which are being made. Another
advantage to using a serverless architecture is that your application can effectively scale to any size in a very
short amount of time without any human intervention. The aforementioned features make serverless ideal for startup projects. 
With that being said, migrating a JAWS application to serverful architecture is as simple as swapping in a
new Guice module. In fact, JAWS applications can be run 100% locally right out of the box using the gradle `serve` task.


## Caveats
- Streaming HTTP requests/responses is not supported and is currently outside the scope of this project. JAWS is ideal 
REST applications which deal in discrete payloads such as JSON objects.
- Unlike API Gateway and Lambda, RDS Aurora Serverless is not eligible for AWS free-tier. This means that JAWS applications
are not completely free to host if you use the  relational-database.

## What's Included
- [Gradle](https://github.com/gradle/gradle) for task automation.
- [AWS CDK](https://aws.amazon.com/cdk/) for cloud deployment.
- [Guice](https://github.com/google/guice) for dependency injection.
- [Rex](https://github.com/duncpro/Rex) for REST endpoints.
- [JRoute](https://github.com/duncpro/JRoute) for URL routing.
- [Jackson](https://github.com/FasterXML/jackson) for JSON serialization.
- [TypeScript](https://github.com/microsoft/TypeScript) / [Jest](https://github.com/facebook/jest) for API integration testing.
- [Slf4j](https://github.com/qos-ch/slf4j) and [Log4j2](https://logging.apache.org/log4j/2.x/) for logging.
- [Jackal](https://github.com/duncpro/Jackal) for making SQL queries.
- [H2](https://github.com/h2database/h2database) for local database testing.
- [typescript-generator](https://github.com/vojtechhabarta/typescript-generator) for generating TypeScript interfaces
from Jackson POJOs.
- [Jest](https://github.com/facebook/jest) for integration testing API endpoints.  

## Features
- Deploy to AWS and run integration tests with a single Gradle task`:integration-tests:run`
- Full support for offline local development. Just use the gradle task `serve` to run the dev server and
  `:integration-tests:runIntegrationTestsLocally` to run your tests against that server.
- Automatic discovery of request handler methods when their classes are bound
as Guice singletons and annotated with `@RestApi`.
- Other useful Gradle tasks like `deploy` and `destroy` which make interacting
with AWS CDK a breeze.
- Preconfigured RDS Aurora Serverless database.
- Run code before the Lambda exits using injectable shutdown hook utility.
- Automatically generated TypeScript equivalent for all DTOs. A DTO is any POJO declared within
a package matching the glob `"**.dto.*"`. See the integration test suite for example usage
  of automatically generated DTOs.


## Getting Started
To get started simply clone the repository...
```bash
git clone https://github.com/duncpro/jaws-rest
```

### Start the Development Server
JAWS-REST includes support for testing your API locally using Java's built-in HttpServer.
```bash
gradle serve
```
### Run API Integration Tests Locally
Out-of-the-box this template comes equipped with a single REST endpoint,
and an integration test for that endpoint.

The integration test suite can be run locally and against the AWS cloud deployment.
To run locally use the following command...
```bash
gradle :integration-test:runIntegrationTestsLocally
```
### Deploy to AWS & Run Integration Tests
This task will build your application, deploy it to AWS, and then run the integration test suite
against it.
```bash
gradle :integration-test:runIntegrationTests
```
If you've never used AWS CDK before you will need to install it and run `cdk boostrap` before this step.
### Add Another Endpoint
JAWS uses AWS API Gateway [Proxy Integration](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-set-up-simple-proxy.html),
which allows a single Lambda function to service multiple HTTP endpoints.

This means that adding a new endpoint is as simple as declaring a new
[Rex](https://github.com/duncpro/Rex) request handler method.

```java
@HttpEndpoint(HttpMethod.GET)
@HttpResource(route = "/ping")
public HttpResponse<String> handlePingRequest() {
    return new HttpResponse(HttpStatus.OK, "pong");
}
```
[Rex](https://github.com/duncpro/Rex) is a lightweight alternative to JAX-RS. Make sure to check its documentation to be
sure it suits your use case.

### Endpoint Registration via Guice
Classes which contain endpoint methods should be eagerly registered with Guice.
```java
class MainModule extends AbstractModule {
    @Override
    public void configure() {
        /* .. */
        bind(MyNewEndpoints.class).asEagerSingleton();
    }
}
```
When a singleton is bound, JAWS scans the class declaration for a `RestApi`
annotation.
```java
@RestApi
class MyEndpoints {}
```
If this annotation is present Guice will automatically register the object with Rex.
### Shutdown Hooks
AWS provides no documentation describing the behavior of `Runtime.addShutdownHook` in Lambda containers.
JAWS remedies this by providing an injectable utility class `AWSLambdaRuntime` which can be used as an alternative.
These shutdown hooks are run immediately before the HTTP response is returned from the Lambda function.
```java
@Singleton
class MyDatabaseService {
    @Inject
    MyDatabaseService(AWSLambdaRuntime runtime) {
        runtime.addShutdownHook(() -> /* close db connection */)
    }
}
```
These hooks are also run by the local development server.

### AutoClosable Shutdown Hooks
By default JAWS registers a binding-listener called `SingletonCloser` with Guice. This class will automatically register
a shutdown hook that calls `AutoClosable#close` for each `Singleton` class implementing `AutoClosable` that is bound
with Guice.
