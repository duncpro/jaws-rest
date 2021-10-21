package com.duncpro.pets;

import com.duncpro.pets.directory.PetDirectoryModule;
import com.duncpro.rex.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(MainModule.class);

    @Override
    public void configure() {
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bind(HttpIntegrator.class).toProvider(HttpIntegratorProvider.class);

        // Features
        install(new PetDirectoryModule());
    }
}
