package com.duncpro.pets.directory;

import com.duncpro.jackal.RelationalDatabaseException;
import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.rex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

@RestApi
@HttpResource(route = "/pets")
@SuppressWarnings("unused")
public class PetDirectoryRestApi {
    private final PetDirectory petDirectory;

    @Inject
    public PetDirectoryRestApi(PetDirectory petDirectory) {
        this.petDirectory = petDirectory;
    }

    @HttpResource(route = "/{petName}/owner")
    @HttpEndpoint(HttpMethod.GET)
    public HttpResponse<LookupOwnerResponseBody> handleLookupOwnerRequest(@PathParam("petName") String petName) {
        try {
            final LookupOwnerResponseBody responseBody = new LookupOwnerResponseBody(
                    petDirectory.lookupOwner(petName).orElse(null));
            return new HttpResponse<>(HttpStatus.OK, responseBody);
        } catch (RelationalDatabaseException e) {
            logger.error("An unexpected database error occurred.", e);
            return new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @HttpEndpoint(HttpMethod.POST)
    public HttpResponse<Void> handleAddPetRequest(@RequestBody AddPetRequestBody pet) {
        try {
            petDirectory.addPet(pet.petName, pet.owner);
            return new HttpResponse<>(HttpStatus.OK);
        } catch (RelationalDatabaseException e) {
            logger.error("An unexpected database error occurred.", e);
            return new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(PetDirectoryRestApi.class);
}
