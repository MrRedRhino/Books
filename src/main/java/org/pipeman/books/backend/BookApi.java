package org.pipeman.books.backend;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.pipeman.books.BookIndex;
import org.pipeman.books.ai.AI;
import org.pipeman.books.utils.Utils.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookApi {
    public static void getBook(Context ctx) {
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        try {
            ctx.json(BookIndex.INSTANCE.books().get(book).serialize());
            return;
        } catch (Exception ignored) {
        }
        throw new NotFoundResponse();
    }

    public static void listBooks(Context ctx) {
        List<Map<String, ?>> books = new ArrayList<>();
        for (BookIndex.Book book : BookIndex.INSTANCE.books().values()) {
            books.add(book.serialize());
        }
        ctx.json(books);
    }

    public static void getPage(Context ctx) {
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        int page = ctx.pathParamAsClass("page", Integer.class).get();

        ctx.html(BookIndex.INSTANCE.getHtml(book, page));
    }

    public static void getSummary(Context ctx) {
        int book = ctx.pathParamAsClass("book", Integer.class).get();
        int page = ctx.pathParamAsClass("page", Integer.class).get();

        ctx.result(AI.getSummary(book, page));
    }

    public static void askAi(Context ctx) {
        int book = ctx.queryParamAsClass("book", Integer.class).get();

        int lower = ctx.queryParamAsClass("lower-page", Integer.class).get();
        int upper = ctx.queryParamAsClass("upper-page", Integer.class).get();
        String question = ctx.body();

        Range range = Range.of(lower, upper);
        if (range.upper() - range.lower() > 2) {
            throw new BadRequestResponse("Too many pages in range");
        }

        if (question.isBlank() || question.length() > 200) {
            throw new BadRequestResponse("Body too long");
        }

        ctx.result(AI.getAnswer(question.trim(), book, range));
    }
}
