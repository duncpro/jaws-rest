package com.duncpro.jaws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public abstract class LNTRequestHandler<I, O> implements RequestHandler<I, O> {
    @Override
    public final O handleRequest(I input, Context context) {
        final var runtime = new AWSLambdaRuntime(context);

        final var response = handleRequest(input, context, runtime);

        runtime.runShutdownHooks();

        return response;
    }

    public abstract O handleRequest(I input, Context context, AWSLambdaRuntime runtime);
}
