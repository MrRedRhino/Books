package org.pipeman.books.search;

import io.javalin.http.Context;
import org.pipeman.books.utils.Utils;
import org.pipeman.books.search.SearchParser.CompletionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchApi {
    private static final SearchParser completer = new SearchParser();

    public static void handleSearch(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null) {
            ctx.status(400).json(Map.of("missing-query-params", new String[]{"query"}));
            return;
        }

        List<Map<String, ?>> completions = new ArrayList<>();
        for (CompletionResult completion : completer.getCompletions(Utils.substr(query, 35))) {
            completions.add(Map.of("page", completion.page(), "book", completion.book().serialize()));
        }
        ctx.json(completions);
    }
}
