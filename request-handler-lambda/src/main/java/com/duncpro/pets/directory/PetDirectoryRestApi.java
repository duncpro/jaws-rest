package com.duncpro.pets;

import com.duncpro.jaws.RestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.rex.*;

@RestApi
@HttpResource(route = "/pets")
@SuppressWarnings("unused")
public class PetDirectoryRestApi {

    public

    @HttpResource(route = "/{petId}/owner")
    @HttpEndpoint(HttpMethod.POST)
    public HttpResponse<LookupOwnerResponse> handleGetPetRequest(@PathParam("petName") String petName) {

        return new HttpResponse<>(HttpStatus.OK, pet);
    }
}
