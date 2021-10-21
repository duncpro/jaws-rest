package com.duncpro.jaws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import com.duncpro.jroute.router.Router;
import com.duncpro.pets.MainModule;
import com.duncpro.pets.RemoteDeploymentModule;
import com.duncpro.rex.*;
import com.google.inject.Guice;
import com.google.inject.Inject;

import java.util.Optional;

/**
 * An instance of this class is instantiated by AWS upon cold start of a new AWS Lambda VM.
 * If you move this class make sure to update the AWS CDK script as well.
 */
@SuppressWarnings("unused")
public class AWSLambdaEntryPoint extends LNTRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context,
                                                      AWSLambdaRuntime runtime) {
        return Guice.createInjector(new AWSLambdaIntegrationModule(runtime), new RexIntegrationModule(), new MainModule(),
                new RemoteDeploymentModule())
                .getInstance(RootRequestHandler.class)
                .compose(this::convertRequest)
                .andThen(this::convertResponse)
                .apply(apiGatewayRequest);
    }

    /**
     * Converts an external AWS API Gateway Request into an internal {@link Rex} {@link HttpRequest}.
     */
    private HttpRequest convertRequest(APIGatewayProxyRequestEvent apiGatewayRequest) {
        return new HttpRequest(
                apiGatewayRequest.getMultiValueHeaders(),
                apiGatewayRequest.getQueryStringParameters(),
                Optional.ofNullable(apiGatewayRequest.getBody())
                        .map(String::getBytes)
                        .orElse(null),
                new Path(apiGatewayRequest.getPath()),
                HttpMethod.valueOf(apiGatewayRequest.getHttpMethod())
        );
    }

    /**
     * Converts an internal {@link Rex} {@link HttpResponse} into an external AWS API Gateway Response.
     */
    private APIGatewayProxyResponseEvent convertResponse(SerializedHttpResponse rexResponse) {
        final var apiGatewayResponse = new APIGatewayProxyResponseEvent();

        if (rexResponse.getBody().isPresent()) {
            apiGatewayResponse.setBody(new String(rexResponse.getBody().get()));
        }

        apiGatewayResponse.setStatusCode(rexResponse.getStatusCode());

        return apiGatewayResponse;
    }
}
