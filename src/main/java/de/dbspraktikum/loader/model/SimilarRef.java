package de.dbspraktikum.loader.model;

// Immutable Daten-Klasse zum Speichern von ähnlichen Produktreferenzn
public record SimilarRef(String sourceProduct, String similarProduct, String source) {
}
