package com.duncpro.pets;

import com.duncpro.pets.directory.PetDirectoryModule;
import com.duncpro.rex.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

public class MainModule extends AbstractModule {
    @Override
    public void configure() {
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bind(HttpIntegrator.class).toProvider(HttpIntegratorProvider.class);

        // Features
        // For every new feature consider creating a new Guice module and installing it here.
        install(new PetDirectoryModule());
    }
}
