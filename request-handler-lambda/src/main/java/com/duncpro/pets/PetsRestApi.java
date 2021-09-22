package com.duncpro.pets;

import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.rex.*;

@RestApi
@HttpResource(route = "/pets")
@SuppressWarnings("unused")
public class PetsRestApi {
    public static class Pet {
        public final String petId;
        Pet(String petId) {
            this.petId = petId;
        }
    }

    @HttpResource(route = "/{petId}")
    @HttpEndpoint(HttpMethod.GET)
    public HttpResponse<Pet> handleGetPetRequest(@PathParam("petId") String petId) {
        final var pet = new Pet(petId);
        return new HttpResponse<>(HttpStatus.OK, pet);
    }
}
