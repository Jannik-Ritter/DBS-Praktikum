package de.dbspraktikum.loader.importer;

import de.dbspraktikum.loader.app.ImportContext;
import de.dbspraktikum.loader.error.Errors;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Importer {
    protected final ImportContext context;

    protected Importer(ImportContext context) {
        this.context = context;
    }

    public abstract void importFile(Path path) throws Exception;

    protected final boolean requireFile(Path path) {
        if (Files.exists(path)) {
            return true;
        }
        context.errors().record("Datei", "Pfad", path.toString(), path.toString(), Errors.FILE_NOT_FOUND);
        return false;
    }
}
