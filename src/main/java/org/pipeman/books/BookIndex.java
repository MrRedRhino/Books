package org.pipeman.books;

import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BookIndex {
    public static final BookIndex INSTANCE = new BookIndex();
    private final Map<Integer, Book> books = new HashMap<>();

    public BookIndex() {
        JSONObject booksObj = Utils.tryThis(() -> new JSONObject(Files.readString(Path.of("books.json"))));
        for (String key : booksObj.keySet()) {
            int id = Integer.parseInt(key);
            books.put(id, Book.fromJSON(id, booksObj.getJSONObject(key)));
        }
    }

    public Map<Integer, Book> books() {
        return books;
    }

    public record Book(int id, String[] searchTerms, String title, String subject, int pageCount) {
        public static Book fromJSON(int id, JSONObject json) {
            return new Book(id,
                    Utils.toStringArray(json.getJSONArray("search-terms")),
                    json.getString("title"),
                    json.getString("subject"),
                    json.getInt("page-count")
            );
        }

        public Map<String, ?> serialize() {
            return Map.of(
                    "id", id(),
                    "title", title(),
                    "subject", subject(),
                    "page-count", pageCount()
            );
        }
    }
}
