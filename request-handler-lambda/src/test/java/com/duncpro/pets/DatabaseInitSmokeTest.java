package com.duncpro.pets;

import com.duncpro.pets.local.LocalRelationalDatabaseProvider;
import com.duncpro.jaws.AWSLambdaRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * This test ensures that the database can be successfully initialized (tables created, etc.),
 * at least locally.
 */
public class DatabaseInitSmokeTest {
    private ExecutorService statementExecutor;
    private AWSLambdaRuntime mockLambdaRuntime;

    @BeforeEach
    public void createStatementExecutor() {
        this.statementExecutor = Executors.newSingleThreadExecutor();
    }

    @BeforeEach
    public void createMockLambdaRuntime() {
        this.mockLambdaRuntime = new AWSLambdaRuntime(() -> Integer.MAX_VALUE);
    }

    @Test
    public void canInitializeDatabaseWithoutThrowing() {
        assertDoesNotThrow(() -> {
            new LocalRelationalDatabaseProvider(mockLambdaRuntime, statementExecutor)
                    .get();
        });
    }

    @AfterEach
    public void cleanup() {
        mockLambdaRuntime.runShutdownHooks();
        this.statementExecutor.shutdown();
    }
}
