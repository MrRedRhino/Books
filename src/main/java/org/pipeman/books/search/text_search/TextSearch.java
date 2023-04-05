package org.pipeman.books.search.text_search;

import info.debatty.java.stringsimilarity.Damerau;
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
    private static final Damerau spellchecker = new Damerau();
    private static final float THRESHOLD = 2.5f;
    private final Map<Integer, Index> indexes = new HashMap<>();
    private final Map<Integer, List<Utils.Range>> pagePositions = new HashMap<>();

    public TextSearch() {
        String indexPath = "indexes/";
        new File(indexPath).mkdirs();

        long start = System.nanoTime();
        for (Map.Entry<Integer, BookIndex.Book> e : BookIndex.INSTANCE.books().entrySet()) {
            try {
                loadIndex(e.getKey(), e.getValue().pageCount(), indexPath);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        System.out.println("Prepared indexes in " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }

    private void loadIndex(int bookId, int pageCount, String indexPath) throws IOException {
        String file = indexPath + "book" + bookId + ".idx";
        Index index;
        try {
            index = Indexer.readIndex(file);
            System.out.println("Read index file " + file);
        } catch (Exception ex) {
            System.out.println("Failed to read index file: " + ex.getMessage() + ". Creating it...");
            index = Indexer.indexText(TextExtractor.getText(bookId));
            Indexer.writeIndex(index, file);
        }
        indexes.put(bookId, index);
        pagePositions.put(bookId, TextExtractor.getPagePositions(bookId, pageCount));
    }

    private int distance(String s1, String s2) {
        final int s2len = s2.length();
        final String shortenedS1 = Utils.substr(s1, s2len);
        return Math.abs(shortenedS1.length() - s2len) > 2 ? 100 : (int) (spellchecker.distance(shortenedS1, s2));
    }

    public List<SearchResult> search(String query, int bookId, Sorting sorting) {
        final Index index = indexes.get(bookId);

        String[] split = query.split(" ");
        List<Result> matches = getMatching(split[0], index);
        if (split.length > 1) matches = getOccurrences(matches, split, 1, index);

        if (sorting == Sorting.SIMILARITY) Collections.sort(matches);

        List<SearchResult> out = new ArrayList<>(matches.size());
        for (Result result : matches) {
            int page = Utils.binarySearch(pagePositions.get(bookId), result.pos);
            Utils.Range range = pagePositions.get(bookId).get(page);
            int offset = result.pos - range.lower();

            Utils.Pair<String, Highlight> preview = createPreview(result.i, index, result.length);
            out.add(new SearchResult(page, preview.v2(), preview.v1(), new Highlight(offset, result.length)));
        }

        if (sorting == Sorting.LOCATION) Collections.sort(out);
        return out;
    }

    private Utils.Pair<String, Highlight> createPreview(int i, Index idx, int queryLength) {
        final int wordCount = idx.getWordCount();
        StringBuilder out = new StringBuilder();
        if (i - 2 >= 0) out.append(idx.getWord(i - 2)).append(' ');
        if (i - 1 >= 0) out.append(idx.getWord(i - 1)).append(' ');
        int start = out.length();
        out.append(idx.getWord(i)).append(' ');
        if (i + 1 < wordCount) out.append(idx.getWord(i + 1)).append(' ');
        if (i + 2 < wordCount) out.append(idx.getWord(i + 2));

        return new Utils.Pair<>(out.toString(), new Highlight(start, queryLength));
    }

    private List<Result> getOccurrences(List<Result> source, String[] query, int indexInQuery, Index idx) {
        List<Result> occurrences = new ArrayList<>();
        String q = query[indexInQuery];
        for (Result result : source) {
            final int i = result.i() + indexInQuery;
            if (i >= idx.getWordCount()) continue;
            String word = idx.getWord(i);
            int distance = distance(word, q);
            if (distance < THRESHOLD) {
                int length = result.length + word.length() + 1;
                occurrences.add(new Result(result.i, result.pos, distance + result.distance, length));
            }
        }
        if (indexInQuery == query.length - 1) return occurrences;
        return getOccurrences(occurrences, query, indexInQuery + 1, idx);
    }

    private List<Result> getMatching(String word, Index idx) {
        Set<String> words = idx.getWords();
        List<Result> out = new ArrayList<>((int) (words.size() * 0.18507385));
        for (String w : words) {
            final int d = distance(w, word);
            if (d < THRESHOLD)
                for (WordOccurrence wo : idx.getPositions(w)) out.add(new Result(wo.i(), d, wo.position(), w.length()));
        }
        return out;
    }

    private record Result(int i, int pos, int distance, int length) implements Comparable<Result> {
        @Override
        public int compareTo(@NotNull Result o) {
            return Integer.compare(distance, o.distance);
        }
    }

    public record SearchResult(int page, Highlight previewHighlight, String preview,
                               Highlight pageHighlight) implements SearchParser.ICompletionResult, Comparable<SearchResult> {
        @Override
        public Map<String, ?> serialize() {
            return Map.of(
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

        @Override
        public int compareTo(@NotNull SearchResult o) {
            return page == o.page ? Integer.compare(pageHighlight.start, o.pageHighlight.start) : Integer.compare(page, o.page);
        }
    }

    public record Highlight(int start, int length) {
    }

}