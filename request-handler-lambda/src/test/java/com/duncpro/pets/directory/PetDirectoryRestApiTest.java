package com.duncpro.pets.directory;

import com.duncpro.rex.Rex;
import org.junit.jupiter.api.Test;

public class PetDirectoryRestApiTest {
    @Test
    public void requestHandlersAreValid() {
        Rex.validateHandlers(PetDirectoryRestApi.class);
    }
}
