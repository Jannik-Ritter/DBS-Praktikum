-- Alte Objekte löschen
DROP VIEW IF EXISTS "ÄhnlicheProdukteBidirektional" CASCADE;
DROP VIEW IF EXISTS "ProduktDurchschnittsRating" CASCADE;
DROP TABLE IF EXISTS "Kundenrezension" CASCADE;
DROP TABLE IF EXISTS "Kaufposition" CASCADE;
DROP TABLE IF EXISTS "Kauf" CASCADE;
DROP TABLE IF EXISTS "Kunde" CASCADE;
DROP TABLE IF EXISTS "Angebot" CASCADE;
DROP TABLE IF EXISTS "Konditionen" CASCADE;
DROP TABLE IF EXISTS "Filiale" CASCADE;
DROP TABLE IF EXISTS "Beteiligte Künstler" CASCADE;
DROP TABLE IF EXISTS "Beteiligte Personen" CASCADE;
DROP TABLE IF EXISTS "Buchautoren" CASCADE;
DROP TABLE IF EXISTS "Ähnliche Produkte" CASCADE;
DROP TABLE IF EXISTS "Produktähnlichkeit" CASCADE;
DROP TABLE IF EXISTS "ProduktKategorie" CASCADE;
DROP TABLE IF EXISTS "Produktkategorien" CASCADE;
DROP TABLE IF EXISTS "Kategorie" CASCADE;
DROP TABLE IF EXISTS "Lied" CASCADE;
DROP TABLE IF EXISTS "Musik-CD" CASCADE;
DROP TABLE IF EXISTS "DVD" CASCADE;
DROP TABLE IF EXISTS "Buch" CASCADE;
DROP TABLE IF EXISTS "Produkt" CASCADE;
DROP TABLE IF EXISTS "Künstler" CASCADE;
DROP TABLE IF EXISTS "Director" CASCADE;
DROP TABLE IF EXISTS "Creator" CASCADE;
DROP TABLE IF EXISTS "Autor" CASCADE;
DROP TABLE IF EXISTS "Actor" CASCADE;
DROP TABLE IF EXISTS "Person" CASCADE;
DROP TABLE IF EXISTS "Label" CASCADE;
DROP TABLE IF EXISTS "Verlag" CASCADE;
-- Referenztabellen
CREATE TABLE "Verlag" (
    "VerlagID" INTEGER NOT NULL PRIMARY KEY,
    "Verlagname" VARCHAR(255) NOT NULL
);
CREATE TABLE "Label" (
    "LabelID" INTEGER NOT NULL PRIMARY KEY,
    "Labelname" VARCHAR(255) NOT NULL
);
-- Personen und Rollen
CREATE TABLE "Person" (
    "PersonID" INTEGER NOT NULL PRIMARY KEY,
    "Vorname" VARCHAR(255) NOT NULL,
    "Nachname" VARCHAR(255) NOT NULL
);
CREATE TABLE "Actor" (
    "PersonID" INTEGER NOT NULL PRIMARY KEY,
    CONSTRAINT "actor_personid_fk" FOREIGN KEY ("PersonID") REFERENCES "Person" ("PersonID")
);
CREATE TABLE "Autor" (
    "PersonID" INTEGER NOT NULL PRIMARY KEY,
    CONSTRAINT "autor_personid_fk" FOREIGN KEY ("PersonID") REFERENCES "Person" ("PersonID")
);
CREATE TABLE "Creator" (
    "PersonID" INTEGER NOT NULL PRIMARY KEY,
    CONSTRAINT "creator_personid_fk" FOREIGN KEY ("PersonID") REFERENCES "Person" ("PersonID")
);
CREATE TABLE "Director" (
    "PersonID" INTEGER NOT NULL PRIMARY KEY,
    CONSTRAINT "director_personid_fk" FOREIGN KEY ("PersonID") REFERENCES "Person" ("PersonID")
);
CREATE TABLE "Künstler" (
    "PersonID" INTEGER NOT NULL PRIMARY KEY,
    CONSTRAINT "kuenstler_personid_fk" FOREIGN KEY ("PersonID") REFERENCES "Person" ("PersonID")
);
-- Produkte
CREATE TABLE "Produkt" (
    "Produktnummer" INTEGER NOT NULL PRIMARY KEY,
    "Titel" VARCHAR(255) NOT NULL,
    -- Könnte man noch unique machen, aber das wäre evtl. zu streng
    "Verkaufsrang" INTEGER NOT NULL,
    "Bild" BYTEA NULL,
    -- Jedes Produkt muss von einem bestimmten Typ sein. Evtl zu streng?
    "Produkttyp" VARCHAR(20) NOT NULL,
    CONSTRAINT "produkt_typ_check" CHECK ("Produkttyp" IN ('Buch', 'Musik-CD', 'DVD'))
);
CREATE TABLE "Buch" (
    "Produktnummer" INTEGER NOT NULL PRIMARY KEY,
    "Seitenzahl" INTEGER NOT NULL,
    "Erscheinungsdatum" DATE NOT NULL,
    "ISBN-Nummer" VARCHAR(20) NOT NULL,
    "VerlagID" INTEGER NOT NULL,
    CONSTRAINT "buch_seitenzahl_check" CHECK ("Seitenzahl" > 0),
    CONSTRAINT "buch_isbn_nummer_unique" UNIQUE ("ISBN-Nummer"),
    CONSTRAINT "buch_produktnummer_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer"),
    CONSTRAINT "buch_verlagid_fk" FOREIGN KEY ("VerlagID") REFERENCES "Verlag" ("VerlagID")
);
CREATE TABLE "Musik-CD" (
    "Produktnummer" INTEGER NOT NULL PRIMARY KEY,
    "LabelID" INTEGER NOT NULL,
    "Erscheinungsdatum" DATE NOT NULL,
    CONSTRAINT "musik_cd_produktnummer_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer"),
    CONSTRAINT "musik_cd_labelid_fk" FOREIGN KEY ("LabelID") REFERENCES "Label" ("LabelID")
);
CREATE TABLE "Lied" (
    "LiedID" INTEGER NOT NULL PRIMARY KEY,
    "Produktnummer" INTEGER NOT NULL,
    "Name" VARCHAR(255) NOT NULL,
    CONSTRAINT "lied_produktnummer_name_unique" UNIQUE ("Produktnummer", "Name"),
    CONSTRAINT "lied_produktnummer_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Musik-CD" ("Produktnummer")
);
CREATE TABLE "DVD" (
    "Produktnummer" INTEGER NOT NULL PRIMARY KEY,
    "Format" VARCHAR(255) NOT NULL,
    "Laufzeit" INTERVAL NOT NULL,
    "Region Code" INTEGER NOT NULL,
    CONSTRAINT "dvd_laufzeit_check" CHECK ("Laufzeit" > INTERVAL '0 seconds'),
    CONSTRAINT "dvd_region_code_check" CHECK ("Region Code" >= 0),
    CONSTRAINT "dvd_produktnummer_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer")
);
-- Kategorien
CREATE TABLE "Kategorie" (
    "KategorieID" INTEGER NOT NULL PRIMARY KEY,
    "Name" VARCHAR(255) NOT NULL,
    "OberkategorieID" INTEGER NULL,
    -- Oberkategorie kann nicht sich selbst referenzieren
    CONSTRAINT "kategorie_oberkategorie_not_self_check" CHECK (
        "OberkategorieID" IS NULL
        OR "OberkategorieID" <> "KategorieID"
    ),
    -- Überprüfen der Fremdschlüsselbedingung bis zum Commit aufschieben, damit Eltern und Kindreferenzen in beliebiger Reihenfolge innerhalb einer Transaktion eingefügt werden können
    CONSTRAINT "kategorie_oberkategorie_fk" FOREIGN KEY ("OberkategorieID") REFERENCES "Kategorie" ("KategorieID") DEFERRABLE INITIALLY DEFERRED
);
CREATE TABLE "Produktkategorien" (
    "Produktnummer" INTEGER NOT NULL,
    "KategorieID" INTEGER NOT NULL,
    PRIMARY KEY ("Produktnummer", "KategorieID"),
    CONSTRAINT "produktkategorien_kategorie_fk" FOREIGN KEY ("KategorieID") REFERENCES "Kategorie" ("KategorieID"),
    CONSTRAINT "produktkategorien_produkt_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer")
);
-- Beziehungen
CREATE TABLE "Buchautoren" (
    "BuchID" INTEGER NOT NULL,
    "AutorID" INTEGER NOT NULL,
    PRIMARY KEY ("BuchID", "AutorID"),
    CONSTRAINT "buchautoren_autorid_fk" FOREIGN KEY ("AutorID") REFERENCES "Autor" ("PersonID"),
    CONSTRAINT "buchautoren_buchid_fk" FOREIGN KEY ("BuchID") REFERENCES "Buch" ("Produktnummer")
);
CREATE TABLE "Beteiligte Personen" (
    "Produktnummer" INTEGER NOT NULL,
    "PersonID" INTEGER NOT NULL,
    "Rolle" VARCHAR(50) NOT NULL,
    PRIMARY KEY ("Produktnummer", "PersonID", "Rolle"),
    CONSTRAINT "beteiligte_personen_rolle_check" CHECK ("Rolle" IN ('Actor', 'Creator', 'Director')),
    CONSTRAINT "beteiligte_personen_person_fk" FOREIGN KEY ("PersonID") REFERENCES "Person" ("PersonID"),
    CONSTRAINT "beteiligte_personen_produkt_fk" FOREIGN KEY ("Produktnummer") REFERENCES "DVD" ("Produktnummer")
);
CREATE TABLE "Beteiligte Künstler" (
    "Produktnummer" INTEGER NOT NULL,
    "KünstlerID" INTEGER NOT NULL,
    PRIMARY KEY ("Produktnummer", "KünstlerID"),
    CONSTRAINT "beteiligte_kuenstler_kuenstler_fk" FOREIGN KEY ("KünstlerID") REFERENCES "Künstler" ("PersonID"),
    CONSTRAINT "beteiligte_kuenstler_produkt_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Musik-CD" ("Produktnummer")
);
CREATE TABLE "Produktähnlichkeit" (
    "Produktnummer1" INTEGER NOT NULL,
    "Produktnummer2" INTEGER NOT NULL,
    PRIMARY KEY ("Produktnummer1", "Produktnummer2"),
    CONSTRAINT "produktaehnlichkeit_order_check" CHECK ("Produktnummer1" < "Produktnummer2"),
    CONSTRAINT "produktaehnlichkeit_p1_fk" FOREIGN KEY ("Produktnummer1") REFERENCES "Produkt" ("Produktnummer"),
    CONSTRAINT "produktaehnlichkeit_p2_fk" FOREIGN KEY ("Produktnummer2") REFERENCES "Produkt" ("Produktnummer")
);
-- Einzelhandel
CREATE TABLE "Filiale" (
    "FilialeID" INTEGER NOT NULL PRIMARY KEY,
    "Name" VARCHAR(255) NOT NULL,
    "Anschrift" VARCHAR(255) NOT NULL,
    "Ort" VARCHAR(255) NOT NULL
);
CREATE TABLE "Konditionen" (
    "FilialeID" INTEGER NOT NULL,
    "Produktnummer" INTEGER NOT NULL,
    "Zustand" VARCHAR(255) NOT NULL,
    "Preis" NUMERIC(10, 2) NULL,
    PRIMARY KEY ("FilialeID", "Produktnummer", "Zustand"),
    CONSTRAINT "konditionen_preis_check" CHECK (
        "Preis" IS NULL
        OR "Preis" >= 0
    ),
    CONSTRAINT "konditionen_filiale_fk" FOREIGN KEY ("FilialeID") REFERENCES "Filiale" ("FilialeID"),
    CONSTRAINT "konditionen_produkt_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer")
);
-- Verkauf und Rezensionen
CREATE TABLE "Kunde" (
    "KundeID" INTEGER NOT NULL PRIMARY KEY,
    "Vorname" VARCHAR(255) NOT NULL,
    "Nachname" VARCHAR(255) NOT NULL,
    "Lieferadresse" VARCHAR(255) NOT NULL,
    "Kontonummer" VARCHAR(50) NOT NULL,
    CONSTRAINT "kunde_kontonummer_unique" UNIQUE ("Kontonummer")
);
CREATE TABLE "Kauf" (
    "KaufID" INTEGER NOT NULL PRIMARY KEY,
    "KundeID" INTEGER NOT NULL,
    "Zeitpunkt" TIMESTAMP NOT NULL,
    CONSTRAINT "kauf_kunde_fk" FOREIGN KEY ("KundeID") REFERENCES "Kunde" ("KundeID")
);
CREATE TABLE "Kaufposition" (
    "KaufID" INTEGER NOT NULL,
    "PositionID" INTEGER NOT NULL,
    "FilialeID" INTEGER NOT NULL,
    "Produktnummer" INTEGER NOT NULL,
    "Zustand" VARCHAR(255) NOT NULL,
    "Kaufpreis" NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY ("KaufID", "PositionID"),
    CONSTRAINT "kaufposition_kaufpreis_check" CHECK ("Kaufpreis" >= 0),
    CONSTRAINT "kaufposition_kauf_fk" FOREIGN KEY ("KaufID") REFERENCES "Kauf" ("KaufID"),
    CONSTRAINT "kaufposition_konditionen_fk" FOREIGN KEY ("FilialeID", "Produktnummer", "Zustand") REFERENCES "Konditionen" ("FilialeID", "Produktnummer", "Zustand")
);
CREATE TABLE "Kundenrezension" (
    "RezensionID" INTEGER NOT NULL PRIMARY KEY,
    "KundeID" INTEGER NOT NULL,
    "Produktnummer" INTEGER NOT NULL,
    "Text" TEXT NOT NULL,
    "Punkte" INTEGER NOT NULL,
    CONSTRAINT "kundenrezension_punkte_check" CHECK (
        "Punkte" BETWEEN 1 AND 5
    ),
    -- Jeder Kunde kann nur eine Rezension für ein Produkt abgeben
    CONSTRAINT "kundenrezension_kunde_produkt_unique" UNIQUE ("KundeID", "Produktnummer"),
    CONSTRAINT "kundenrezension_kunde_fk" FOREIGN KEY ("KundeID") REFERENCES "Kunde" ("KundeID"),
    CONSTRAINT "kundenrezension_produkt_fk" FOREIGN KEY ("Produktnummer") REFERENCES "Produkt" ("Produktnummer")
);
-- Views
CREATE VIEW "ProduktDurchschnittsRating" AS
SELECT p."Produktnummer",
    AVG(k."Punkte") AS "DurchschnittsRating"
FROM "Produkt" p
    LEFT JOIN "Kundenrezension" k ON k."Produktnummer" = p."Produktnummer"
GROUP BY p."Produktnummer";
CREATE VIEW "ÄhnlicheProdukteBidirektional" AS
SELECT "Produktnummer1" AS "Produktnummer",
    "Produktnummer2" AS "ÄhnlicheProduktnummer"
FROM "Produktähnlichkeit"
UNION
SELECT "Produktnummer2" AS "Produktnummer",
    "Produktnummer1" AS "ÄhnlicheProduktnummer"
FROM "Produktähnlichkeit";