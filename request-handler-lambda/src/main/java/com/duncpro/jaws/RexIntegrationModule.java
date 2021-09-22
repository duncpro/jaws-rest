package com.duncpro.jaws;

import com.duncpro.jroute.router.Router;
import com.duncpro.jroute.router.TreeRouter;
import com.duncpro.rex.HttpIntegrator;
import com.duncpro.rex.JavaMethodRequestHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;

public class RexIntegrationModule extends AbstractModule {
    private final Router<JavaMethodRequestHandler> router = new TreeRouter<>();

    @Override
    public void configure() {
        requireBinding(HttpIntegrator.class);
        bindListener(Matchers.any(), new RestEndpointRegistrar(router));
    }

    @Provides
    public Router<JavaMethodRequestHandler> provideRouter() { return router; }
}
