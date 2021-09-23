# JAWS-REST
Boilerplate Gradle project for developing serverless RESTful API's on AWS using Java.

JAWS is ideal for REST APIs which deal in relatively small and discrete payloads such as JSON objects.
Streaming HTTP requests/responses is not supported and is currently outside the scope of this project.

## What's Included
- [Gradle](https://github.com/gradle/gradle) for task automation.
- [AWS CDK](https://aws.amazon.com/cdk/) for deployment.
- [Guice](https://github.com/google/guice) for dependency injection.
- [Rex](https://github.com/duncpro/Rex) for REST endpoints.
- [JRoute](https://github.com/duncpro/JRoute) for URL routing.
- [Jackson](https://github.com/FasterXML/jackson) for JSON serialization.
- [TypeScript](https://github.com/microsoft/TypeScript) / [Jest](https://github.com/facebook/jest) for API integration testing.

## Features
- Run code before the Lambda exits using shutdown hooks.
- Automatic discovery of request handler methods when their classes are bound
as Guice singletons and annotated with `@RestApi`.
- Deploy to AWS and run integration tests with a single Gradle task`:integration-tests:run`
- Full support for offline local development. Just use the gradle task `serve` to run the dev server and
`:integration-tests:runLocal` to run your tests against that server.
- Other useful Gradle tasks like `deploy` and `destroy` which make interacting
with AWS CDK a breeze.


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
gradle :integration-test:runLocal
```
### Deploy to AWS & Run Integration Tests
This task will build your application, deploy it to AWS, and then run the integration test suite
against it.
```bash
gradle :integration-test:run
```
### Add Another Endpoint
Since JAWS uses AWS API Gateway [Proxy Integration](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-set-up-simple-proxy.html)
and Rex, adding a new endpoint is as simple
as declaring a new method.

```java
// Just add a snippet like this to com.duncpro.pets.PetsRestApi
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
#### Shutdown Hook Example Usage
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
