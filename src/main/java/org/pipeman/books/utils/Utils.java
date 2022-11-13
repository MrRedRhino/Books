package org.pipeman.books.utils;

import org.json.JSONArray;

import java.util.*;

public class Utils {
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>(list.size());
        for (Map.Entry<K, V> entry : list) result.put(entry.getKey(), entry.getValue());

        return result;
    }
        // https://stackoverflow.com/questions/49900588/edit-distance-java
        // alternative: https://github.com/raelgc/java-spell-checker/
        // https://stackoverflow.com/questions/24968697/how-to-implement-auto-suggest-using-lucenes-new-analyzinginfixsuggester-api
        // https://www.baeldung.com/lucene

    public static String substr(String in, int end) {
        return in.substring(0, Math.min(in.length(), end));
    }

    public static <T> T getOrElse(T thing, T alt) {
        return thing == null ? alt : thing;
    }

    public static String[] toStringArray(JSONArray input) {
        String[] out = new String[input.length()];
        for (int i = 0; i < input.length(); i++) out[i] = input.getString(i);
        return out;
    }

    public static String[] toArray(List<String> input) {
        String[] out = new String[input.size()];
        for (int i = 0; i < input.size(); i++) out[i] = input.get(i);
        return out;
    }

    public static <T> List<T> sublist(List<T> input, int len) {
        return input.subList(0, Math.min(input.size(), len));
    }

    public static <T> T tryThis(Producer<T> action) {
        try {
            return action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int binarySearch(List<Range> ranges, int pos) {
        int low = 0;
        int high = ranges.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Range midVal = ranges.get(mid);

            if (midVal.isInRange(pos)) return mid;
            else if (midVal.lower() > pos) high = mid - 1;
            else low = mid + 1;
        }
        return -1;
    }

    @FunctionalInterface
    public interface Producer<T> {
        T run() throws Exception;
    }

    public record Range(int lower, int upper) {

        public static Range of(int i1, int i2) {
            return new Range(Math.min(i1, i2), Math.max(i1, i2));
        }

        public boolean isInRange(int i) {
            return i >= lower && i <= upper;
        }
    }

    public record Pair<T, T1>(T v1, T1 v2) {
    }
}
