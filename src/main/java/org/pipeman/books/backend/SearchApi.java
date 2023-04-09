package org.pipeman.books.backend;

import io.javalin.http.Context;
import org.pipeman.books.BookIndex;
import org.pipeman.books.search.SearchParser;
import org.pipeman.books.search.text_search.Sorting;
import org.pipeman.books.search.text_search.TextSearch;
import org.pipeman.books.search.text_search.TextSearch.SearchResult;
import org.pipeman.books.utils.Utils;

import java.util.*;

public class SearchApi {
    private static final SearchParser COMPLETER = new SearchParser();

    public static void completions(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null) {
            ctx.status(400).json(Map.of("missing-query-params", new String[]{"query"}));
            return;
        }

        List<Map<String, ?>> completions = new ArrayList<>();
        for (SearchParser.CompletionResult completion : COMPLETER.getCompletions(Utils.substr(query, 35))) {
            completions.add(completion.serialize());
        }
        ctx.json(completions);
    }

    public static void search(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null) {
            ctx.status(400).json(Map.of("missing-query-params", new String[]{"query"}));
            return;
        }
        Optional<Integer> book = Utils.parseInt(ctx.queryParam("book"));
        if (book.isEmpty() || BookIndex.INSTANCE.books().get(book.get()) == null) {
            ctx.status(400).json(Map.of("missing-query-params", new String[]{"book"}));
            return;
        }

        Optional<Integer> page = Utils.parseInt(ctx.queryParam("page"));
        Optional<Integer> index = Utils.parseInt(ctx.queryParam("index"));

        List<SearchResult> searchResults = SearchEngineProvider.getEngine().search(query, book.get(), Sorting.LOCATION);
        List<SearchResult> outputResults;
        if (page.isPresent()) {
            searchResults.removeIf(result -> result.page() != page.get());
            outputResults = searchResults;
        } else if (index.isPresent()) {
            outputResults = getResultsWithIndex(searchResults, index.get());
        } else {
            outputResults = List.of();
        }
        ctx.json(constructSearchResult(searchResults.size(), outputResults));
    }

    private static List<SearchResult> getResultsWithIndex(List<SearchResult> results, int idx) {
        return idx >= results.size() ? List.of() : Collections.singletonList(results.get(idx));
    }

    public static void loadSearchEngine() {
        //noinspection ResultOfMethodCallIgnored
        SearchEngineProvider.getEngine();
    }

    private static Map<String, ?> constructSearchResult(int totalResults, List<SearchResult> results) {
        List<Map<String, ?>> serialized = new ArrayList<>();
        for (SearchResult completion : results) serialized.add(completion.serialize());
        return Map.of(
                "total-results", totalResults,
                "results", serialized
        );
    }

    private static class SearchEngineProvider {
        private static final TextSearch ENGINE = new TextSearch();

        public static TextSearch getEngine() {
            return ENGINE;
        }
    }
}
