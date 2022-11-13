package org.pipeman.books.search.text_search;

import info.debatty.java.stringsimilarity.Levenshtein;
import org.jetbrains.annotations.NotNull;
import org.pipeman.books.BookIndex;
import org.pipeman.books.converter.TextExtractor;
import org.pipeman.books.search.SearchParser;
import org.pipeman.books.search.text_search.index.Index;
import org.pipeman.books.search.text_search.index.Index.WordOccurrence;
import org.pipeman.books.search.text_search.index.Indexer;
import org.pipeman.books.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TextSearch {
    public static final TextSearch INSTANCE = Utils.tryThis(TextSearch::new);
    private final List<Index> indexes = new ArrayList<>();
    private final Map<Integer, List<Utils.Range>> pagePositions = new HashMap<>();
    //    private final JaroWinkler JW = new JaroWinkler(0.1);
    private final Levenshtein JW = new Levenshtein();
    private final int THRESHOLD = 3;
    private static final int MAX_TOTAL_RESULTS = 25;
    private static final int MAX_BOOK_RESULTS = 10;

    public TextSearch() {
        String INDEX_PATH = "indexes/";
        new File(INDEX_PATH).mkdirs();

        long start = System.nanoTime();
        for (Map.Entry<Integer, BookIndex.Book> e : BookIndex.INSTANCE.books().entrySet()) {
            try {
                loadIndex(e.getKey(), e.getValue().pageCount(), INDEX_PATH);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        System.out.println("Prepared indexes in " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }

    private void loadIndex(int bId, int pageCount, String indexPath) throws IOException {
        String file = indexPath + "book" + bId + ".idx";
        Index index;
        try {
            index = Indexer.readIndex(file);
            System.out.println("Read index file " + file);
        } catch (Exception ex) {
            System.out.println("Failed to read index file: " + ex.getMessage() + ". Creating it...");
            index = Indexer.indexText(TextExtractor.getText(bId));
            Indexer.writeIndex(index, file);
        }
        indexes.add(index);
        pagePositions.put(bId, TextExtractor.getPagePositions(bId, pageCount));
    }

    private int distance(String s1, String s2) {
        final int s2len = s2.length();
        final String shortenedS1 = Utils.substr(s1, s2len);
        return Math.abs(shortenedS1.length() - s2len) > 2 ? 100 : (int) (JW.distance(shortenedS1, s2, THRESHOLD));
    }

    public List<SearchResult> search(String query) {
        int qLength = query.length();
//        if (qLength <= 2) return List.of();

        String[] q = query.split(" ");
        List<Result> results = new ArrayList<>();

        for (int i = 0; i < BookIndex.INSTANCE.books().size(); i++) {
            Index idx = indexes.get(i);
            List<Result> r = getMatching(q[0], idx, i);
            if (q.length > 1) r = getOccurrences(r, q, 1, idx);
            Collections.sort(r);
            results.addAll(Utils.sublist(r, MAX_BOOK_RESULTS));
        }

        Collections.sort(results);
        List<SearchResult> out = new ArrayList<>(MAX_TOTAL_RESULTS);
        final int iterations = Math.min(results.size(), MAX_TOTAL_RESULTS);
        for (int i = 0; i < iterations; i++) {
            Result result = results.get(i);
            int book = result.bookId;
            int page = Utils.binarySearch(pagePositions.get(book), result.pos);
            Utils.Range range = pagePositions.get(book).get(page);
            int offset = result.pos - range.lower();

            Utils.Pair<String, Highlight> preview = createPreview(result.i, indexes.get(book), qLength);
            out.add(new SearchResult(book, page, preview.v2(), preview.v1(), new Highlight(offset, qLength)));
        }

        return out;
    }

    private Utils.Pair<String, Highlight> createPreview(int i, Index idx, int queryLength) {
        final int wc = idx.getWordCount();
        StringBuilder out = new StringBuilder();
        if (i - 2 >= 0) out.append(idx.getWord(i - 2)).append(' ');
        if (i - 1 >= 0) out.append(idx.getWord(i - 1)).append(' ');
        int start = out.length();
        String word = idx.getWord(i);
        out.append(word).append(' ');
        if (i + 1 < wc) out.append(idx.getWord(i + 1)).append(' ');
        if (i + 2 < wc) out.append(idx.getWord(i + 2));

        return new Utils.Pair<>(out.toString(), new Highlight(start, queryLength));
    }

    private List<Result> getOccurrences(List<Result> source, String[] query, int indexInQuery, Index idx) {
        List<Result> result = new ArrayList<>();
        String q = query[indexInQuery];
        for (Result e : source) {
            final int i = e.i() + indexInQuery;
            if (i > idx.getWordCount()) continue;
            int d = distance(idx.getWord(i), q);
            if (d < THRESHOLD) result.add(new Result(e.bookId, e.i, e.pos, d + e.distance));
        }
        if (indexInQuery == query.length - 1) return result;
        return getOccurrences(result, query, indexInQuery + 1, idx);
    }

    private List<Result> getMatching(String word, Index idx, int bookId) {
        int max = word.length() == 1 ? MAX_BOOK_RESULTS : -1;
        Set<String> words = idx.getWords();
        List<Result> out = new ArrayList<>((int) (max == -1 ? words.size() * 0.18507385 : max));
        int count = 0;
        for (String w : words) {
            if (max != -1 && ++count > max) break;
            final int d = distance(w, word);
            if (d < THRESHOLD)
                for (WordOccurrence wo : idx.getPositions(w)) out.add(new Result(bookId, wo.i(), d, wo.position()));
        }
        return out;
    }

    public void init() {
    }

    private record Result(int bookId, int i, int pos, int distance) implements Comparable<Result> {
        @Override
        public int compareTo(@NotNull Result o) {
            return Integer.compare(distance, o.distance);
        }
    }

    public record SearchResult(int bookId, int page, Highlight previewHighlight, String preview,
                               Highlight pageHighlight) implements SearchParser.ICompletionResult {
        @Override
        public Map<String, ?> serialize() {
            return Map.of(
                    "type", "TEXT_SEARCH",
                    "book", BookIndex.INSTANCE.books().get(bookId).serialize(),
                    "page", page,
                    "preview", Map.of(
                            "preview", preview,
                            "start", previewHighlight.start,
                            "length", previewHighlight.length
                    ),
                    "highlight", Map.of(
                            "start", pageHighlight.start,
                            "length", pageHighlight.length
                    )
            );
        }
    }

    public record Highlight(int start, int length) {
    }
}
