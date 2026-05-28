package de.dbspraktikum.loader;

import de.dbspraktikum.loader.app.LoaderApp;
import de.dbspraktikum.loader.config.LoaderConfig;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        new LoaderApp(LoaderConfig.from(args)).run();
    }
}
