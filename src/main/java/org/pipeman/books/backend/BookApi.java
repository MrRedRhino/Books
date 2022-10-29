package org.pipeman.books.backend;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.pipeman.books.BookIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class BookApi {
    public static void getBook(Context ctx) {
        try {
            ctx.json(BookIndex.INSTANCE.books().get(Integer.parseInt(ctx.pathParam("book"))).serialize());
            return;
        } catch (Exception ignored) {
        }
        throw new NotFoundResponse();
    }

    public static void listBooks(Context ctx) {
        ctx.html("Liste mit büchern");
    }

    public static void getPage(Context ctx) throws IOException {
        String book = ctx.pathParam("book");
        String page = ctx.pathParam("page");

        try {
            ctx.html(Files.readString(Path.of("book-data", "html", book, page + ".html")));
        } catch (NoSuchFileException ignored) {
            throw new NotFoundResponse();
        }
    }
}
