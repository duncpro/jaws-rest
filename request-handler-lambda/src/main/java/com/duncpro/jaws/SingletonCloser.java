package com.duncpro.jaws.rest;

import com.google.inject.Scopes;
import com.google.inject.spi.ProvisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for provisions of {@link AutoCloseable} {@link javax.inject.Singleton} and registers
 * a shutdown hook with {@link AWSLambdaRuntime} invoking the {@link AutoCloseable#close()} member method.
 *
 * An instance of {@link AWSLambdaRuntime} can be acquired by implementing {@link LNTLambdaRequestHandler}.
 */
public class SingletonCloser implements ProvisionListener  {
    private final AWSLambdaRuntime awsLambdaRuntime;

    public SingletonCloser(AWSLambdaRuntime awsLambdaRuntime) {
        this.awsLambdaRuntime = awsLambdaRuntime;
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        final var provisionedObj = provision.provision();

        final var isSingleton = Scopes.isSingleton(provision.getBinding());
        final var isCloseable = AutoCloseable.class.isAssignableFrom(provisionedObj.getClass());

        if (isSingleton && isCloseable) {
            awsLambdaRuntime.addShutdownHook(() ->
                    close((AutoCloseable) provisionedObj));
        }
    }

    private void close(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            logger.error("Unhandled error occurred while running shutdown hook", e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SingletonCloser.class);
}
