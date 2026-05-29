package de.dbspraktikum.loader.model;

// Immutable Daten-Klasse zum Speichern von ähnlichen Produktreferenzen
public record SimilarRef(String sourceProduct, String similarProduct, String similarTitle, String source) {
}
