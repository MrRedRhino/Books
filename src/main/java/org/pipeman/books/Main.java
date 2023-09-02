package org.pipeman.books;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.pipeman.books.backend.BookApi;
import org.pipeman.books.backend.BookUploadApi;
import org.pipeman.books.backend.SearchApi;
import org.pipeman.books.converter.Converter;
import org.pipeman.pconf.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static final ConfigProvider<Config> CFG = ConfigProvider.of("config.properties", Config::new);
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        boolean textSearch = false;
        if (args.length == 1) {
            switch (args[0]) {
                case "-add-book" -> {
                    Converter.main(args);
                    System.exit(0);
                }
                case "-enable-text-search" -> textSearch = true;
                default -> throw new RuntimeException("Unrecognised option " + args[0]);
            }
        }
        LOGGER.info("Starting Books!");

        Javalin app = Javalin.create(c -> {
            c.showJavalinBanner = false;
            c.staticFiles.add(cfg -> {
                cfg.location = Location.EXTERNAL;
                cfg.directory = "book-data/images/";
                cfg.hostedPath = "/images/books";
            });

            c.staticFiles.add("static", Location.EXTERNAL);
        }).start(CFG.c().port);

        app.routes(() -> {
            get("", ctx -> ctx.html(Files.readString(Path.of("static", "index.html"))));
            get("upload", ctx -> ctx.html(Files.readString(Path.of("static", "upload.html"))));

            path("api", () -> {
                get("completions", SearchApi::completions);

                path("books", () -> {
                    get("", BookApi::listBooks);
                    get("{book}", BookApi::getBook);
                    get("{book}/{page}", BookApi::getPage);
                    get("{book}/{page}/summary", BookApi::getSummary);
                });

                // Falls Dir die KI-Features gefallen, würde ich mich über eine Spende (hier) sehr freuen, da diese
                // Features verhältnismäßig teuer im Unterhalt sind.

                get("ask-ai", BookApi::askAi);

                post("new-book", BookUploadApi::handleUpload);
            });
        });

        if (textSearch) {
            LOGGER.info("Starting text search");
            app.routes(() -> get("api/text-search", SearchApi::search));
            SearchApi.loadSearchEngine();
        }
        LOGGER.info("Books started!");
    }
}