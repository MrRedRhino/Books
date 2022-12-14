package org.pipeman.books;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.pipeman.books.backend.BookApi;
import org.pipeman.books.backend.BookUploadApi;
import org.pipeman.books.backend.SearchApi;
import org.pipeman.books.converter.Converter;
import org.pipeman.pconf.ConfigProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static final ConfigProvider<Config> CFG = ConfigProvider.of("config.properties", Config::new);

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("-add-book")) {
            Converter.main(args);
            System.exit(0);
        }

        Javalin app = Javalin.create(c -> {
            c.showJavalinBanner = false;
            c.staticFiles.add(cfg -> {
                cfg.location = Location.EXTERNAL;
                cfg.directory = "book-data/images/";
                cfg.hostedPath = "/images/books";
            });
        }).start(CFG.c().port);

        app.routes(() -> {
            get("", ctx -> ctx.html(Files.readString(Path.of("static", "index.html"))));
            get("upload", ctx -> ctx.html(Files.readString(Path.of("static", "upload.html"))));

            path("api", () -> {
                get("search", SearchApi::handleSearch);

                path("books", () -> {
                    get("", BookApi::listBooks);
                    get("{book}", BookApi::getBook);
                    get("{book}/{page}", BookApi::getPage);
                });

                post("new-book", BookUploadApi::handleUpload);
            });
        });
    }
}