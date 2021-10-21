package com.duncpro.pets;

import com.duncpro.jackal.RelationalDatabase;
import com.duncpro.jackal.RelationalDatabaseException;
import com.duncpro.jackal.jdbc.DataSourceWrapper;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
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

    @BindingAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StatementExecutor {}

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
        });
        return statementExecutor;
    }

    @Provides
    @Singleton
    public RelationalDatabase provideRelationalDatabase(AWSLambdaRuntime runtime,
                                                        @StatementExecutor ExecutorService statementExecutor) {
        final JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:~/test;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");

        try (final var reader = Files.newBufferedReader(Path.of(System.getenv("DB_INIT_SCRIPT")))){
            RunScript.execute(h2.getConnection(), reader);
        } catch (SQLException e) {
            logger.error("An unexpected SQL error occurred while initializing the local database." +
                    " Some application features might not work properly.", e);
        } catch (IOException e) {
            logger.warn("Did not run database initialization SQL script because it could not be read from the" +
                    " file system.", e);
        }

        final var db = new DataSourceWrapper(h2, statementExecutor);
        runtime.addShutdownHook(() -> {
            try {
                db.prepareStatement("SHUTDOWN").executeUpdate();
            } catch (RelationalDatabaseException e) {
                logger.error("Error occurred while shutting down the local H2 database.", e);
            }
        });
        return db;
    }
}
