-- Auto-generated and ugly
CREATE TABLE "Produkt" (
  "Produktnummer" INTEGER PRIMARY KEY NOT NULL,
  "Titel" "VARCHAR(255)" NOT NULL,
  "Rating" "NUMERIC(3,2)" NOT NULL,
  "Verkaufsrang" INTEGER UNIQUE NOT NULL,
  "Bild" JSON
);
CREATE TABLE "Buch" (
  "Produktnummer" INTEGER PRIMARY KEY NOT NULL,
  "Seitenzahl" INTEGER NOT NULL,
  "Erscheinungsdatum" DATE NOT NULL,
  "ISBN-Nummer" INTEGER UNIQUE NOT NULL,
  "VerlagID" INTEGER NOT NULL
);
CREATE TABLE "Musik-CD" (
  "Produktnummer" INTEGER PRIMARY KEY NOT NULL,
  "LabelID" INTEGER NOT NULL,
  "Erscheinungsdatum" DATE NOT NULL,
  "Titel" "VARCHAR(255)[]" NOT NULL
);
CREATE TABLE "DVD" (
  "Produktnummer" INTEGER PRIMARY KEY NOT NULL,
  "Format" "VARCHAR(255)" NOT NULL CHECK ("Format" IN('')),
  "Laufzeit" "TIME(0)" NOT NULL,
  "Region Code" INTEGER NOT NULL
);
CREATE TABLE "Buchautoren" (
  "BuchID" INTEGER NOT NULL,
  "AutorID" INTEGER NOT NULL,
  PRIMARY KEY ("BuchID", "AutorID")
);
CREATE TABLE "Verlag" (
  "VerlagID" INTEGER PRIMARY KEY NOT NULL,
  "Verlagname" "VARCHAR(255)" NOT NULL
);
CREATE TABLE "Beteiligte Personen" (
  "Produktnummer" INTEGER NOT NULL,
  "PersonID" INTEGER NOT NULL,
  PRIMARY KEY ("Produktnummer", "PersonID")
);
CREATE TABLE "Person" (
  "PersonID" INTEGER PRIMARY KEY NOT NULL,
  "Vorname" "VARCHAR(255)" NOT NULL,
  "Nachname" "VARCHAR(255)" NOT NULL
);
CREATE TABLE "Autor" ("PersonID" INTEGER PRIMARY KEY NOT NULL);
CREATE TABLE "Actor" ("PersonID" INTEGER PRIMARY KEY NOT NULL);
CREATE TABLE "Creator" ("PersonID" INTEGER PRIMARY KEY NOT NULL);
CREATE TABLE "Director" ("PersonID" INTEGER PRIMARY KEY NOT NULL);
CREATE TABLE "Label" (
  "LabelID" INTEGER PRIMARY KEY NOT NULL,
  "Labelname" "VARCHAR(255)" NOT NULL
);
CREATE TABLE "Künstler" ("id" INTEGER PRIMARY KEY NOT NULL);
CREATE TABLE "Beteiligte Künstler" (
  "Produktnummer" INTEGER NOT NULL,
  "KünstlerID" INTEGER NOT NULL,
  PRIMARY KEY ("Produktnummer", "KünstlerID")
);
CREATE TABLE "Produktkategorien" (
  "Produktnummer" INTEGER NOT NULL,
  "KategorieID" "VARCHAR(255)" NOT NULL,
  PRIMARY KEY ("Produktnummer", "KategorieID")
);
CREATE TABLE "Kategorie" (
  "KategorieID" INTEGER PRIMARY KEY NOT NULL,
  "Name" "VARCHAR(255)" NOT NULL,
  "OberkategorieID" INTEGER
);
CREATE TABLE "Produktähnlichkeit" (
  "Produktnummer1" INTEGER NOT NULL,
  "Produktnummer2" Integer NOT NULL,
  PRIMARY KEY ("Produktnummer1", "Produktnummer2")
);
CREATE TABLE "Konditionen" (
  "Produktnummer" INTEGER NOT NULL,
  "FillialID" INTEGER NOT NULL,
  "Preis" "NUMERIC(10,2)" NOT NULL,
  "Verfügbarkeit" BOOL NOT NULL,
  "Zustand" "ENUM" NOT NULL,
  PRIMARY KEY ("Produktnummer", "FillialID")
);
CREATE TABLE "Filliale" (
  "FillialID" INTEGER PRIMARY KEY NOT NULL,
  "AdressID" INTEGER NOT NULL,
  "Name" "VARCHAR(255)" NOT NULL
);
CREATE TABLE "Adresse" (
  "AdressID" INTEGER PRIMARY KEY NOT NULL,
  "Land" "VARCHAR(255)" NOT NULL,
  "Ort" "VARCHAR(255)" NOT NULL,
  "Postleitzahl" "VARCHAR(255)" NOT NULL,
  "Straße" "VARCHAR(255)" NOT NULL,
  "Hausnummer" "VARCHAR(255)" NOT NULL
);
CREATE TABLE "Kauf" (
  "KaufID" INTEGER PRIMARY KEY NOT NULL,
  "KonditionsID" INTEGER NOT NULL,
  "LieferadressID" INTEGER NOT NULL
);
ALTER TABLE "Person"
ADD CONSTRAINT "person_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Actor" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Beteiligte Personen"
ADD CONSTRAINT "beteiligte personen_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Director" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "DVD"
ADD CONSTRAINT "dvd_produktnummer_foreign" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Beteiligte Personen"
ADD CONSTRAINT "beteiligte personen_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Creator" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Produkt"
ADD CONSTRAINT "produkt_produktnummer_foreign" FOREIGN KEY ("Produktnummer") REFERENCES "Buch" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Person"
ADD CONSTRAINT "person_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Creator" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Beteiligte Künstler"
ADD CONSTRAINT "beteiligte künstler_produktnummer_foreign" FOREIGN KEY ("Produktnummer") REFERENCES "Musik-CD" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Person"
ADD CONSTRAINT "person_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Autor" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Person"
ADD CONSTRAINT "person_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Director" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Beteiligte Personen"
ADD CONSTRAINT "beteiligte personen_produktnummer_foreign" FOREIGN KEY ("Produktnummer") REFERENCES "DVD" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Buch"
ADD CONSTRAINT "buch_verlagid_foreign" FOREIGN KEY ("VerlagID") REFERENCES "Verlag" ("VerlagID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Person"
ADD CONSTRAINT "person_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Künstler" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Buchautoren"
ADD CONSTRAINT "buchautoren_autorid_foreign" FOREIGN KEY ("AutorID") REFERENCES "Autor" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Musik-CD"
ADD CONSTRAINT "musik_cd_labelid_foreign" FOREIGN KEY ("LabelID") REFERENCES "Label" ("LabelID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Buchautoren"
ADD CONSTRAINT "buchautoren_buchid_foreign" FOREIGN KEY ("BuchID") REFERENCES "Buch" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Beteiligte Künstler"
ADD CONSTRAINT "beteiligte künstler_künstlerid_foreign" FOREIGN KEY ("KünstlerID") REFERENCES "Künstler" ("id") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Musik-CD"
ADD CONSTRAINT "musik_cd_produktnummer_foreign" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Beteiligte Personen"
ADD CONSTRAINT "beteiligte personen_personid_foreign" FOREIGN KEY ("PersonID") REFERENCES "Actor" ("PersonID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Produktkategorien"
ADD FOREIGN KEY ("KategorieID") REFERENCES "Kategorie" ("KategorieID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Produktkategorien"
ADD FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Kategorie"
ADD FOREIGN KEY ("OberkategorieID") REFERENCES "Kategorie" ("KategorieID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Produktähnlichkeit"
ADD FOREIGN KEY ("Produktnummer2") REFERENCES "Produkt" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Produktähnlichkeit"
ADD FOREIGN KEY ("Produktnummer1") REFERENCES "Produkt" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Konditionen"
ADD FOREIGN KEY ("FillialID") REFERENCES "Filliale" ("FillialID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Konditionen"
ADD FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Kauf"
ADD FOREIGN KEY ("LieferadressID") REFERENCES "Adresse" ("AdressID") DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE "Konditionen"
ADD FOREIGN KEY ("FillialID", "Produktnummer") REFERENCES "Kauf" ("KonditionsID") DEFERRABLE INITIALLY IMMEDIATE;