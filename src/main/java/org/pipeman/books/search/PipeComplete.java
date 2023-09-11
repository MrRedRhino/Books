package org.pipeman.books.search;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.jetbrains.annotations.NotNull;
import org.pipeman.books.BookIndex;
import org.pipeman.books.BookIndex.Book;
import org.pipeman.books.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipeComplete {
    private final JaroWinkler JW;

    public PipeComplete() {
        JW = new JaroWinkler(0.1);
    }

    private float similarity(String s1, String s2) {
        return Math.abs(s1.length() - s2.length()) > 3 ? 0 : (float) JW.similarity(s1, s2);
    }

    private float getSimilarity(String query, Book book) {
        int length = query.length();
        float similarity = 0;
        for (String word : book.searchTerms()) {
            similarity = Math.max(similarity(Utils.substr(word, length), query), similarity);
        }
        return similarity;
    }

    public List<Completion> getCompletions(String query) {
        String q = query.toLowerCase();
        List<Completion> out = new ArrayList<>();
        for (Book book : BookIndex.INSTANCE.books().values()) {
            out.add(new Completion(book, getSimilarity(q, book)));
        }
        return out;
    }

    public List<Book> getCompletionsSorted(String query) {
        List<Book> out = new ArrayList<>();
        if (query.isBlank()) return List.of();

        List<Completion> completions = getCompletions(query);
        Collections.sort(completions);

        for (Completion entry : completions) {
            if (entry.similarity > 0.6) {
                out.add(entry.book);
            }
            if (out.size() >= 3) break;
        }
        return out;
    }

    public record Completion(Book book, float similarity, float relevance) implements Comparable<Completion> {
        public Completion(Book book, float similarity) {
            this(book, similarity, similarity * book.relevance());
        }

        @Override
        public int compareTo(@NotNull PipeComplete.Completion o) {
            return Float.compare(o.relevance, relevance);
        }
    }
}
