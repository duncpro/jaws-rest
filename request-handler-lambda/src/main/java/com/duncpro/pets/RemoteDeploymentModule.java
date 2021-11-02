package com.duncpro.pets;

import com.duncpro.jackal.SQLDatabase;
import com.duncpro.jackal.aws.AuroraServerlessCredentials;
import com.duncpro.jackal.aws.DefaultAuroraServerlessDatabase;
import com.duncpro.pets.local.LocalDeploymentModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;

/**
 * This module is used in conjunction with {@link MainModule} when running the application on the AWS Cloud.
 * It provides bindings for AWS cloud services such as RDS.
 * This module is complimentary to {@link LocalDeploymentModule}.
 */
public class RemoteDeploymentModule extends AbstractModule {
    @Override
    public void configure() {
        bind(SQLDatabase.class).to(DefaultAuroraServerlessDatabase.class);
    }

    @Provides
    @Singleton
    public DefaultAuroraServerlessDatabase provideRelationalDatabase() {
        final String dbArn = System.getenv("MASTER_DB_ARN");
        final String secretArn = System.getenv("MASTER_DB_SECRET_ARN");
        final var credentials = new AuroraServerlessCredentials(dbArn, secretArn);

        return new DefaultAuroraServerlessDatabase(credentials);
    }
}
