package com.duncpro.pets.directory;

import com.google.inject.AbstractModule;

public class PetDirectoryModule extends AbstractModule {
    @Override
    public void configure() {
        bind(PetDirectoryRestApi.class).asEagerSingleton();
        bind(PetDirectory.class);
    }
}
