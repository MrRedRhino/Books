package org.pipeman.books;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.pipeman.books.converter.Compression;
import org.pipeman.books.converter.Converter;
import org.pipeman.books.search.SearchApi;
import org.pipeman.books.utils.TerminalUtil;
import org.pipeman.pconf.ConfigProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Main {
    public static final ConfigProvider<Config> CFG = ConfigProvider.of("config.properties", Config::new);

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("-add-book")) {
            int bookId;
            File pdf;
            float compressionQuality;

            while (true) {
                System.out.print("Enter the id for the new book (must be an integer)");
                bookId = TerminalUtil.readInt();
                if (BookIndex.INSTANCE.books().get(bookId) == null) break;
                System.out.println("Id is already taken");
            }

            while (true) {
                System.out.print("Enter the path the new pdf file > ");
                File f = new File(TerminalUtil.readLine());
                if (f.exists()) {
                    pdf = f;
                    break;
                }
                System.out.println("File does not exist");
            }

            while (true) {
                try {
                    System.out.print("Enter the compression quality (lower values produce worse results, range 0-1) > ");
                    float f = Float.parseFloat(TerminalUtil.readLine());
                    if (f < 0 || f > 1) System.out.println("Value has to be between 0 and 1");
                    else {
                        compressionQuality = f;
                        break;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            Compression.compressionQuality = compressionQuality;
            Converter.convertBook(bookId, pdf);
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

            path("api", () -> {
                get("search", SearchApi::handleSearch);

                path("books", () -> {
                    get("", BookApi::listBooks);
                    get("{book}", BookApi::getBook);
                    get("{book}/{page}", BookApi::getPage);
                });
            });
        });
    }
}