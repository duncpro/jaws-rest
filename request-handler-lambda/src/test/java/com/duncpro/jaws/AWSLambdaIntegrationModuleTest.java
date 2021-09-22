package com.duncpro.jaws;

import com.amazonaws.services.lambda.runtime.Context;
import com.duncpro.jaws.AWSLambdaIntegrationModule;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AWSLambdaIntegrationModuleTest {
    static class SingletonAutoCloseable implements AutoCloseable {
        volatile boolean isClosed = false;
        @Override
        public void close() {
            isClosed = true;
        }
    }

    static class SingletonModule extends AbstractModule {
        @Provides
        @Singleton
        SingletonAutoCloseable provide() { return new SingletonAutoCloseable(); }
    }

    @Test
    public void doesRegisterShutdownHookForSingletons() {
        final var lambdaRuntime = mock(AWSLambdaRuntime.class);

        final var injector = Guice.createInjector(new AWSLambdaIntegrationModule(lambdaRuntime),
                new SingletonModule());

        injector.getInstance(SingletonAutoCloseable.class);

        verify(lambdaRuntime, times(1)).addShutdownHook(any());
    }

    @Test
    public void doesCloseSingletons() {
        final var context = mock(Context.class);
        when(context.getRemainingTimeInMillis()).thenReturn(Integer.MAX_VALUE);
        final var lambdaRuntime = new AWSLambdaRuntime(context);

        final var injector = Guice.createInjector(new AWSLambdaIntegrationModule(lambdaRuntime),
                new SingletonModule());

        final var singleton = injector.getInstance(SingletonAutoCloseable.class);

        lambdaRuntime.runShutdownHooks();
        assertTrue(singleton.isClosed);
    }
}
