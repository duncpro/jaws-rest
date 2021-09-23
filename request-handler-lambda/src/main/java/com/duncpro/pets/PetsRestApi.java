package com.duncpro.pets;

import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.rex.*;

@RestApi
@HttpResource(route = "/pets")
@SuppressWarnings("unused")
public class PetsRestApi {
    public static class PetResponseBody {
        public final String petId;
        public final String name;
        PetResponseBody(String petId, String name) {
            this.petId = petId;
            this.name = name;
        }
    }

    public static class PetRequestBody {
        public String name;
    }

    @HttpResource(route = "/{petId}")
    @HttpEndpoint(HttpMethod.POST)
    public HttpResponse<PetResponseBody> handleGetPetRequest(@PathParam("petId") String petId,
                                                             @RequestBody PetRequestBody body) {
        final var pet = new PetResponseBody(petId, body.name);
        return new HttpResponse<>(HttpStatus.OK, pet);
    }
}
