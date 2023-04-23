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
        Optional<Location> location = Utils.getEnum(ctx.queryParam("location"), Location.class);

        List<SearchResult> searchResults = SearchEngineProvider.getEngine().search(query, book.get(), Sorting.LOCATION);
        if (page.isPresent()) {
            switch (location.orElse(Location.EXACT)) {
                case BEFORE -> page = Optional.of(getPreviousPage(page.get(), searchResults));
                case AFTER -> page = Optional.of(getNextPage(page.get(), searchResults));
            }

            int minIndex = -1;
            List<SearchResult> output = new ArrayList<>();
            for (int i = 0; i < searchResults.size(); i++) {
                SearchResult result = searchResults.get(i);
                if (result.page() == page.get()) {
                    output.add(result);
                    if (minIndex == -1) minIndex = i;
                }
            }
            ctx.json(constructSearchResult(searchResults.size(), output, minIndex));
        } else if (index.isPresent()) {
            List<SearchResult> withIndex = getResultsWithIndex(searchResults, index.get());
            ctx.json(constructSearchResult(searchResults.size(), withIndex));
        } else {
            ctx.json(constructSearchResult(searchResults.size(), List.of()));
        }
    }

    private static List<SearchResult> getResultsWithIndex(List<SearchResult> results, int idx) {
        return idx >= results.size() ? List.of() : Collections.singletonList(results.get(idx));
    }

    public static void loadSearchEngine() {
        //noinspection ResultOfMethodCallIgnored
        SearchEngineProvider.getEngine();
    }

    private static int getNextPage(int page, List<SearchResult> results) {
        for (SearchResult result : results) {
            if (result.page() > page) return result.page();
        }
        return 1;
    }

    private static int getPreviousPage(int page, List<SearchResult> results) {
        int previous = 1;
        for (SearchResult result : results) {
            if (result.page() >= page) break;
            previous = result.page();
        }
        return previous;
    }

    private static Map<String, ?> constructSearchResult(int totalResults, List<SearchResult> results) {
        return constructSearchResult(totalResults, results, -1);
    }

    private static Map<String, ?> constructSearchResult(int totalResults, List<SearchResult> results, int index) {
        List<Map<String, ?>> serialized = new ArrayList<>();
        for (SearchResult completion : results) serialized.add(completion.serialize());
        return index == -1 ?
                Map.of(
                        "total-results", totalResults,
                        "results", serialized
                ) :
                Map.of(
                        "total-results", totalResults,
                        "results", serialized,
                        "start-index", index
                );
    }

    private static class SearchEngineProvider {
        private static final TextSearch ENGINE = new TextSearch();

        public static TextSearch getEngine() {
            return ENGINE;
        }
    }

    public enum Location {
        BEFORE,
        EXACT,
        AFTER
    }
}
