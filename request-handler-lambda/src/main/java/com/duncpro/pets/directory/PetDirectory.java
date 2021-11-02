package com.duncpro.pets.directory;

import com.duncpro.jackal.SQLDatabase;
import com.duncpro.jackal.SQLException;

import javax.inject.Inject;
import java.util.Optional;

import static com.duncpro.jackal.InterpolatableSQLStatement.sql;

public class PetDirectory {
    private final SQLDatabase db;

    @Inject
    public PetDirectory(SQLDatabase db) {
        this.db = db;
    }

    public Optional<String> lookupOwner(String petName) throws SQLException {
        return sql("SELECT owner FROM pet WHERE pet_name = ?;")
                .withArguments(petName)
                .executeQuery(db)
                .findFirst()
                .flatMap(row -> row.get("owner", String.class));
    }

    public void addPet(String petName, String owner) throws SQLException {
        sql("INSERT INTO pet (pet_name, owner) VALUES (?, ?);")
                .withArguments(petName, owner)
                .executeUpdate(db);
    }
}
