package com.example;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import java.util.HashMap;

public class EchoResponseFactory {
    @Inject
    private ObjectMapper json;

    String generateEchoResponseBody(APIGatewayProxyRequestEvent req) {
        final var respBody = new HashMap<String, Object>();

        respBody.put("requestMethod", req.getHttpMethod());
        respBody.put("requestBody", req.getBody());
        respBody.put("requestPath", req.getPath());
        respBody.put("requestQueryParams", req.getQueryStringParameters());

        try {
            return json.writeValueAsString(respBody);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }
}
