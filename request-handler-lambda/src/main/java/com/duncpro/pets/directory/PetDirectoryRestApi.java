package com.duncpro.pets.directory;

import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.rex.*;

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
    public HttpResponse<LookupOwnerResponseBody> handleLookupOwnerRequest(@PathParam("petName") String petName) {
        final var responseBody = new LookupOwnerResponseBody(
                petDirectory.lookupOwner(petName).orElse(null)
        );
        return new HttpResponse<>(HttpStatus.OK, responseBody);
    }

    @HttpEndpoint(HttpMethod.POST)
    public HttpResponse<Void> handleAddPetRequest(@RequestBody AddPetRequestBody pet) {
        petDirectory.addPet(pet.petName, pet.owner);
        return new HttpResponse<>(HttpStatus.OK);
    }

}
