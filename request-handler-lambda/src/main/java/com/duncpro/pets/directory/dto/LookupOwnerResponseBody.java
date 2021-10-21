package com.duncpro.pets.directory.dto;

public class LookupOwnerResponseBody {
    public String owner;
    public boolean didFindOwner;

    public LookupOwnerResponseBody(String owner) {
        this.didFindOwner = owner != null;
        this.owner = owner;
    }
}
