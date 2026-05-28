package de.dbspraktikum.loader.error;

public record LoadError(String entity, String attribute, String rawValue, String source, String message) {
    public String key() {
        return entity + "." + attribute;
    }
}
