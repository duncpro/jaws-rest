package com.example;

import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.rex.HttpEndpoint;
import com.duncpro.rex.HttpResource;
import com.duncpro.rex.PathParam;

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
    public Pet handleGetPetRequest(@PathParam("petId") String petId) {
        return new Pet(petId);
    }
}
