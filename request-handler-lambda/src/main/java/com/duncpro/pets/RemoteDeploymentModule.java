package com.duncpro.pets;

import com.duncpro.jackal.RelationalDatabase;
import com.duncpro.jackal.rds.AmazonDataAPIDatabase;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import software.amazon.awssdk.services.rdsdata.RdsDataAsyncClient;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

/**
 * This module is used in conjunction with {@link MainModule} when running the application on the AWS Cloud.
 * It provides bindings for AWS cloud services such as RDS.
 * This module is complimentary to {@link LocalDeploymentModule}.
 */
public class RemoteDeploymentModule extends AbstractModule {
    @Provides
    @Singleton
    public RelationalDatabase provideRelationalDatabase(AWSLambdaRuntime runtime,
                                                        @TransactionExecutor ExecutorService transactionExecutor) {
        final String dbArn = System.getenv("MASTER_DB_ARN");
        final String secretArn = System.getenv("MASTER_DB_SECRET_ARN");

        final var rds = RdsDataAsyncClient.create();
        runtime.addShutdownHook(rds::close);

        return new AmazonDataAPIDatabase(rds, dbArn, secretArn, transactionExecutor);
    }
}
