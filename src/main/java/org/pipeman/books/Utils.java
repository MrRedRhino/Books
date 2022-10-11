package org.pipeman.books;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) result.put(entry.getKey(), entry.getValue());

        return result;
        // https://stackoverflow.com/questions/49900588/edit-distance-java
        // alternative: https://github.com/raelgc/java-spell-checker/
        // https://stackoverflow.com/questions/24968697/how-to-implement-auto-suggest-using-lucenes-new-analyzinginfixsuggester-api
        // https://www.baeldung.com/lucene
    }

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


    public static <T> T tryThis(Producer<T> action) {
        try {
            return action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface Producer<T> {
        T run() throws Exception;
    }
}
