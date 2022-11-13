package org.pipeman.books.backend;

import io.javalin.http.Context;
import org.pipeman.books.search.SearchParser;
import org.pipeman.books.utils.Utils;

import java.util.*;

public class SearchApi {
    private static final SearchParser completer = new SearchParser();

    public static void handleSearch(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null) {
            ctx.status(400).json(Map.of("missing-query-params", new String[]{"query"}));
            return;
        }

        List<Map<String, ?>> completions = new ArrayList<>();
        for (SearchParser.ICompletionResult completion : completer.getCompletions(Utils.substr(query, 35))) {
            completions.add(completion.serialize());
        }
        ctx.json(completions);
    }
}
