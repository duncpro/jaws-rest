package com.duncpro.pets.directory;

import com.duncpro.jackal.RelationalDatabase;

import javax.inject.Inject;
import java.util.Optional;

public class PetDirectory {
    private final RelationalDatabase db;

    @Inject
    public PetDirectory(RelationalDatabase db) {
        this.db = db;
    }

    public Optional<String> lookupOwner(String petName) {
        try (final var results = db.prepareStatement("SELECT owner FROM pet WHERE pet_name = ?;")
                .withArgument(petName)
                .executeQuery()) {
            return results.findFirst()
                    .flatMap(row -> row.get("owner", String.class));
        }
    }

    public void addPet(String petName, String owner) {
        db.prepareStatement("INSERT INTO pet (pet_name, owner) VALUES (?, ?);")
                .withArguments(petName, owner)
                .executeUpdate()
                .join();
    }
}
