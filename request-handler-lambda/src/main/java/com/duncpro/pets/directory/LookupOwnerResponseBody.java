package com.duncpro.pets;

public class LookupOwnerResponse {
    public String owner;
    public boolean didFindOwner;

    public LookupOwnerResponse(String owner) {
        this.didFindOwner = owner != null;
        this.owner = owner;
    }
}
