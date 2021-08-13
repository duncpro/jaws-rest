package com.duncpro.jaws.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * "Leave No Trace" request handler which provides {@link Runtime#addShutdownHook(Thread)}-like abilities
 * for AWS Lambda functions.
 */
public abstract class LNTLambdaRequestHandler<I, O> implements RequestHandler<I, O> {
    @Override
    public final O handleRequest(I input, Context context) {
        final var runtime = new AWSLambdaRuntime(context);

        final var response = handleRequest(input, context, runtime);

        runtime.runShutdownHooks();

        return response;
    }

    public abstract O handleRequest(I input, Context context, AWSLambdaRuntime runtime);
}
