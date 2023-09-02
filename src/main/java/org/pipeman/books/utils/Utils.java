package org.pipeman.books.utils;

import org.jetbrains.annotations.NotNull;
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

    public static <T> T tryThis(ExceptionSupplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void tryThis(ExceptionRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static <T extends Enum<T>> Optional<T> getEnum(String s, Class<T> enumClass) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(s)) {
                return Optional.of(constant);
            }
        }
        return Optional.empty();
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
    public interface ExceptionSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ExceptionRunnable {
        void run() throws Exception;
    }

    public record Range(int lower, int upper) implements Iterable<Integer> {

        public static Range of(int i1, int i2) {
            return new Range(Math.min(i1, i2), Math.max(i1, i2));
        }

        public boolean isInRange(int i) {
            return i >= lower && i <= upper;
        }

        @NotNull
        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return lower + i <= upper;
                }

                @Override
                public Integer next() {
                    return i++ + lower;
                }
            };
        }
    }

    public static int getDay() {
        return (int) (System.currentTimeMillis() / 86_400_000);
    }

    public record Pair<T, T1>(T v1, T1 v2) {
    }
}
