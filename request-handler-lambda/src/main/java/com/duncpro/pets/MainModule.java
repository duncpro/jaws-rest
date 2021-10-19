package com.duncpro.pets;

import com.duncpro.jackal.RelationalDatabaseException;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.duncpro.pets.directory.PetDirectoryModule;
import com.duncpro.rex.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(MainModule.class);

    @Override
    public void configure() {
        bind(ObjectMapper.class).toInstance(new ObjectMapper());

        // Features
        install(new PetDirectoryModule());
    }

    @Provides
    public HttpIntegrator provideHttpIntegrator(ObjectMapper jackson) {
        final var basicIntegrator = new BasicHttpIntegratorBuilder();
        JavaHttpIntegrations.addAll(basicIntegrator);

        basicIntegrator.registerRequestBodyType("application/json", (type, raw) -> {
            try {
                return jackson.readValue(raw, type);
            } catch (IOException e) {
                throw new ConversionException(e);
            }
        });

        basicIntegrator.registerResponseBodyType("application/json", obj -> {
            try {
                return jackson.writeValueAsString(obj).getBytes();
            } catch (IOException e) {
                throw new ConversionException(e);
            }
        });
        return basicIntegrator.build();
    }

    @Provides
    @Singleton
    public ExecutorService provideTransactionExecutor(AWSLambdaRuntime runtime) {
        final var transactionExecutor = Executors.newCachedThreadPool();
        runtime.addShutdownHook(() -> {
            transactionExecutor.shutdown();
            try {
                transactionExecutor.awaitTermination(runtime.getDurationUntilLambdaTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("One or more database transactions might not have been finalized because the shutdown hook" +
                        " which was awaiting their completion has been interrupted.", e);
            }
        });
        return transactionExecutor;
    }
}
