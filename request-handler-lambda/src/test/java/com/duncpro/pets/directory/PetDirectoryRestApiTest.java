package com.duncpro.pets.directory;

import com.duncpro.pets.HttpIntegratorProvider;
import com.duncpro.rex.Rex;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class PetDirectoryRestApiTest {
    @Test
    public void requestHandlersAreValid() {
        final var mockIntegrator = new HttpIntegratorProvider(new ObjectMapper()).get();
        Rex.validateHandlers(PetDirectoryRestApi.class, mockIntegrator);
    }
}
