package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AWSLambdaEntryPointTest {

    @Test
    void handleRequestRespondsWithStatusCode200() {
        final var request = new APIGatewayProxyRequestEvent();

        final var context = mock(Context.class);
        when(context.getRemainingTimeInMillis()).thenReturn(Integer.MAX_VALUE);

        final var entryPoint = new AWSLambdaEntryPoint();

        final var response = entryPoint.handleRequest(request, context);

        assertEquals(response.getStatusCode(), 200);
    }
}
