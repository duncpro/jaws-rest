package com.duncpro.pets.local;

import com.duncpro.jackal.RelationalDatabase;
import com.duncpro.jackal.RelationalDatabaseException;
import com.duncpro.jackal.jdbc.DataSourceWrapper;
import com.duncpro.jaws.AWSLambdaRuntime;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

/**
 * When running the application locally, a local in-memory database will be used instead of Amazon RDS.
 */
public class LocalRelationalDatabaseProvider implements Provider<RelationalDatabase> {
    private final AWSLambdaRuntime runtime;
    private final ExecutorService statementExecutor;

    @Inject
    public LocalRelationalDatabaseProvider(AWSLambdaRuntime runtime,
                                           @StatementExecutor ExecutorService statementExecutor) {

        this.runtime = runtime;
        this.statementExecutor = statementExecutor;
    }

    @Override
    public RelationalDatabase get() {
        final JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:./local-testing-db;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");

        try (final var reader = Files.newBufferedReader(Path.of(System.getenv("DB_INIT_SCRIPT")))){
            RunScript.execute(h2.getConnection(), reader);
        } catch (SQLException e) {
            throw new RuntimeException("An unexpected SQL error occurred while initializing the local database." +
                    " Some application features might not work properly.", e);
        } catch (IOException e) {
            throw new RuntimeException("Did not run database initialization SQL script because it could not be read from the" +
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

    private static final Logger logger = LoggerFactory.getLogger(LocalRelationalDatabaseProvider.class);
}
