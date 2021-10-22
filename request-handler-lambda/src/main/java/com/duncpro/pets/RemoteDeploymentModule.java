package com.duncpro.pets;

import com.duncpro.jackal.RelationalDatabase;
import com.duncpro.jackal.aws.DefaultAuroraServerlessRelationalDatabase;
import com.duncpro.jaws.AWSLambdaRuntime;
import com.duncpro.pets.local.LocalDeploymentModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import software.amazon.awssdk.services.rdsdata.RdsDataAsyncClient;

import javax.inject.Singleton;

/**
 * This module is used in conjunction with {@link MainModule} when running the application on the AWS Cloud.
 * It provides bindings for AWS cloud services such as RDS.
 * This module is complimentary to {@link LocalDeploymentModule}.
 */
public class RemoteDeploymentModule extends AbstractModule {
    @Override
    public void configure() {
        bind(RelationalDatabase.class).to(DefaultAuroraServerlessRelationalDatabase.class);
    }

    @Provides
    @Singleton
    public DefaultAuroraServerlessRelationalDatabase provideRelationalDatabase(AWSLambdaRuntime runtime) {
        final String dbArn = System.getenv("MASTER_DB_ARN");
        final String secretArn = System.getenv("MASTER_DB_SECRET_ARN");

        final var rds = RdsDataAsyncClient.create();
        runtime.addShutdownHook(rds::close);

        return new DefaultAuroraServerlessRelationalDatabase(dbArn, secretArn);
    }
}
