package com.duncpro.pets.directory;

import com.duncpro.jackal.SQLException;
import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.pets.directory.dto.AddPetRequestBody;
import com.duncpro.pets.directory.dto.LookupOwnerResponseBody;
import com.duncpro.rex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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
    public HttpResponse<LookupOwnerResponseBody> handleLookupOwnerRequest(@PathParam("petName") String petName) throws SQLException {
        final LookupOwnerResponseBody responseBody = new LookupOwnerResponseBody(
                petDirectory.lookupOwner(petName).orElse(null));
        return new HttpResponse<>(HttpStatus.OK, responseBody);

    }

    @HttpEndpoint(HttpMethod.POST)
    public HttpResponse<Void> handleAddPetRequest(@RequestBody AddPetRequestBody pet) throws SQLException {
        petDirectory.addPet(pet.petName, pet.owner);
        return HttpResponse.bodiless(HttpStatus.OK);
    }

    private static final Logger logger = LoggerFactory.getLogger(PetDirectoryRestApi.class);
}
