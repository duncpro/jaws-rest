package com.duncpro.pets.local;

import com.duncpro.jackal.SQLDatabase;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.duncpro.jaws.ShutdownHookPriority;
import com.duncpro.pets.MainModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This module is used in conjunction with {@link MainModule} when running the application locally, such as during
 * integration testing and development. It's responsible for providing mock implementations of cloud services,
 * such as substituting an H2 database for an RDS database.
 */
public class LocalDeploymentModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(LocalDeploymentModule.class);

    @Override
    public void configure() {
        bind(SQLDatabase.class).toProvider(LocalRelationalDatabaseProvider.class).asEagerSingleton();
    }

    /**
     * The AWS Aurora Data API is unavailable when running the application locally. Therefore JDBC in conjunction
     * with a local database is used instead. Unfortunately JDBC does not support CompletableFuture. To bridge
     * the gap caused by lack of async query support inside of JDBC a cached thread pool is used.
     */
    @Provides
    @Singleton
    @StatementExecutor
    public ExecutorService provideStatementExecutor(AWSLambdaRuntime runtime) {
        final var statementExecutor = Executors.newCachedThreadPool();
        runtime.addShutdownHook(() -> {
            statementExecutor.shutdown();
            try {
                statementExecutor.awaitTermination(runtime.getDurationUntilLambdaTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("One or more database operations might not have been completed because the shutdown hook" +
                        " which was awaiting their completion has been interrupted.", e);
            }
        }, ShutdownHookPriority.LATE);
        return statementExecutor;
    }
}
