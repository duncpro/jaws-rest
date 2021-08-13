package com.duncpro.jaws.rest;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class AWSLambdaIntegrationModule extends AbstractModule {
    private final AWSLambdaRuntime runtime;

    public AWSLambdaIntegrationModule(AWSLambdaRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void configure() {
        bind(AWSLambdaRuntime.class).toInstance(runtime);
        bindListener(Matchers.any(), new SingletonCloser(runtime));
    }
}
