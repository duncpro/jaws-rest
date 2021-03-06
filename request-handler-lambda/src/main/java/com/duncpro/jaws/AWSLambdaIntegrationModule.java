package com.duncpro.jaws;

import com.duncpro.jroute.router.Router;
import com.duncpro.jroute.router.TreeRouter;
import com.duncpro.rex.HttpIntegrator;
import com.duncpro.rex.JavaMethodRequestHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
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
