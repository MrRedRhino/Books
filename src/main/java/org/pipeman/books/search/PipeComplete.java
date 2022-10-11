package org.pipeman.books.search;

import org.pipeman.books.BookIndex;
import org.pipeman.books.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeComplete {
    private int distance(String s1, String s2, int i, int j) {
        int lengthDifference = Math.abs(s1.length() - s2.length());
        if (lengthDifference > 2) return lengthDifference * 2;

        if (j == s2.length()) return s1.length() - i;

        if (i == s1.length()) return s2.length() - j;

        if (s1.charAt(i) == s2.charAt(j)) return distance(s1, s2, i + 1, j + 1);
        int rep = distance(s1, s2, i + 1, j + 1) + 1;
//        int del = distance(s1, s2, i, j + 1) + 1;
        int ins = distance(s1, s2, i + 1, j) + 2;
//        return Math.min(del, Math.min(ins, rep));
        return Math.min(rep, ins);
    }

    private int getMinDistance(String query, String[] words) {
        int length = query.length();
        int minDist = Integer.MAX_VALUE;
        for (String word : words) minDist = Math.min(distance(Utils.substr(word, length), query, 0, 0), minDist);
        return minDist;
    }

    public Map<BookIndex.Book, Integer> getCompletions(String query) {
        String q = query.toLowerCase();
        Map<BookIndex.Book, Integer> out = new HashMap<>();
        for (Map.Entry<Integer, BookIndex.Book> bookEntry : BookIndex.INSTANCE.books().entrySet()) {
            out.put(bookEntry.getValue(), getMinDistance(q, bookEntry.getValue().searchTerms()));
        }
        return out;
    }

    public List<BookIndex.Book> getCompletionsSorted(String query) {
        List<BookIndex.Book> out = new ArrayList<>();
        if (query.length() == 0) return List.of();
        for (Map.Entry<BookIndex.Book, Integer> entry : Utils.sortByValue(getCompletions(query)).entrySet()) {
            int errors = entry.getValue();
            if (errors < query.length() && errors < 4) out.add(entry.getKey());
        }
        return out;
    }

    /*
     for (String s : query.split(" ")) {
            String q = s.toLowerCase();
            Map<BookIndex.Book, Integer> qResults = new HashMap<>();
            for (Map.Entry<Integer, BookIndex.Book> bookEntry : BookIndex.INSTANCE.books().entrySet()) {
                qResults.put(bookEntry.getValue(), getMinDistance(q, bookEntry.getValue().searchTerms()));
            }

            int minValue = Integer.MAX_VALUE;
            BookIndex.Book bestBook = null;
            for (Map.Entry<BookIndex.Book, Integer> entry : qResults.entrySet()) {
                if (entry.getValue() < minValue) {
                    minValue = entry.getValue();
                    bestBook = entry.getKey();
                }
            }
            out.put(bestBook, minValue);
        }
     */
}
