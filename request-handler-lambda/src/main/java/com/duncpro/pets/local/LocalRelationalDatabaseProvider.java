package com.duncpro.pets.local;

import com.duncpro.jackal.SQLDatabase;
import com.duncpro.jackal.jdbc.DataSourceWrapper;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.duncpro.jaws.ShutdownHookPriority;
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

import static com.duncpro.jackal.InterpolatableSQLStatement.sql;

/**
 * When running the application locally, a local in-memory database will be used instead of Amazon RDS.
 */
public class LocalRelationalDatabaseProvider implements Provider<SQLDatabase> {
    private final AWSLambdaRuntime runtime;
    private final ExecutorService statementExecutor;

    @Inject
    public LocalRelationalDatabaseProvider(AWSLambdaRuntime runtime,
                                           @StatementExecutor ExecutorService statementExecutor) {

        this.runtime = runtime;
        this.statementExecutor = statementExecutor;
    }

    @Override
    public SQLDatabase get() {
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

        final var db = new DataSourceWrapper(statementExecutor, h2);
        runtime.addShutdownHook(() -> {
            try {
                sql("SHUTDOWN;").executeUpdate(db);
            } catch (com.duncpro.jackal.SQLException e) {
                logger.error("Error occurred while shutting down the local H2 database.", e);
            }
        }, ShutdownHookPriority.EARLY);
        return db;
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalRelationalDatabaseProvider.class);
}
