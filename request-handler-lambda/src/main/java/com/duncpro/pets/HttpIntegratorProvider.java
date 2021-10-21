package com.duncpro.pets;

import com.duncpro.rex.BasicHttpIntegratorBuilder;
import com.duncpro.rex.ConversionException;
import com.duncpro.rex.HttpIntegrator;
import com.duncpro.rex.JavaHttpIntegrations;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

public class HttpIntegratorProvider implements Provider<HttpIntegrator> {
    private final ObjectMapper jackson;

    @Inject
    public HttpIntegratorProvider(ObjectMapper jackson) {
        this.jackson = jackson;
    }

    @Override
    public HttpIntegrator get() {
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
        return basicIntegrator.build();
    }
}
