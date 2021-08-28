package com.example;

import com.duncpro.rex.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.IOException;

public class MainModule extends AbstractModule {
    @Override
    public void configure() {
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bind(PetsRestApi.class).asEagerSingleton();
    }

    @Provides
    public HttpIntegrator provideHttpIntegrator(ObjectMapper jackson) {
        final var rootIntegrator = new HttpIntegratorBuilder();

        final var basicIntegrator = new BasicHttpIntegratorBuilder();
        JavaHttpIntegrations.addAll(basicIntegrator);

        basicIntegrator.registerRequestBodyType("application/json", (type, raw) -> {
            try {
                return jackson.readValue(raw, type);
            } catch (IOException e) {
                throw new ConversionException(e);
            }
        });

        basicIntegrator.registerResponseBodyType("application/json", obj -> {
            try {
                return jackson.writeValueAsString(obj).getBytes();
            } catch (IOException e) {
                throw new ConversionException(e);
            }
        });

        rootIntegrator.inheritFrom(basicIntegrator.build());
        return rootIntegrator.build();
    }
}
