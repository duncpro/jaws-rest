/* This SQL script is responsible for initializing the database (creating tables, etc.) */
/* This script is run by Gradle after every AWS deployment. */
/* Additionally this script will be run on the local H2 database before starting the application locally. */
CREATE TABLE IF NOT EXISTS pet (pet_name VARCHAR NOT NULL, owner VARCHAR NOT NULL);
