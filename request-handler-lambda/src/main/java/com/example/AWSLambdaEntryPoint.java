package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.duncpro.jaws.rest.AWSLambdaIntegrationModule;
import com.duncpro.jaws.rest.AWSLambdaRuntime;
import com.duncpro.jaws.rest.LNTLambdaRequestHandler;
import com.google.inject.Guice;

/**
 * An instance of this class is instantiated by AWS upon cold start of a new AWS Lambda VM.
 * If you move this class make sure to update the AWS CDK script as well.
 */
public class AWSLambdaEntryPoint extends LNTLambdaRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent req, Context context, AWSLambdaRuntime runtime) {

        final var injector = Guice.createInjector(new AWSLambdaIntegrationModule(runtime), new MainModule());

        final var echoResponseFactory = injector.getInstance(EchoResponseFactory.class);

        final var response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(echoResponseFactory.generateEchoResponseBody(req));
        return response;
    }
}
