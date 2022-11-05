package org.pipeman.books.search;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.pipeman.books.BookIndex;
import org.pipeman.books.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeComplete {
    private final JaroWinkler JW;

    public PipeComplete() {
        JW = new JaroWinkler(0.1);
    }

    private int distance(String s1, String s2) {
        return Math.abs(s1.length() - s2.length()) > 2 ? 50 : (int) (JW.distance(s1, s2) * 10);
    }

    private int getMinDistance(String query, String[] words) {
        int length = query.length();
        int minDist = Integer.MAX_VALUE;
        for (String word : words) minDist = Math.min(distance(Utils.substr(word, length), query), minDist);
        return minDist;
    }

    public Map<BookIndex.Book, Integer> getCompletions(String query) {
        String q = query.toLowerCase();
        Map<BookIndex.Book, Integer> out = new HashMap<>();
        for (Map.Entry<Integer, BookIndex.Book> bookEntry : BookIndex.INSTANCE.books().entrySet())
            out.put(bookEntry.getValue(), getMinDistance(q, bookEntry.getValue().searchTerms()));
        return out;
    }

    public List<BookIndex.Book> getCompletionsSorted(String query) {
        List<BookIndex.Book> out = new ArrayList<>();
        if (query.isBlank()) return List.of();

        for (Map.Entry<BookIndex.Book, Integer> entry : Utils.sortByValue(getCompletions(query)).entrySet())
            if (entry.getValue() < query.length() && entry.getValue() < 2) out.add(entry.getKey());
        return out;
    }
}
