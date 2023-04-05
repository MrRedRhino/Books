package org.pipeman.books.search.text_search.index;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record Index(Map<String, List<WordOccurrence>> wos, String[] words) {
    public Set<String> getWords() {
        return wos.keySet();
    }

    public List<Index.WordOccurrence> getPositions(String word) {
        return wos.get(word);
    }

    public String getWord(int i) {
        return words[i];
    }

    public int getWordCount() {
        return words.length;
    }

    public record WordOccurrence(int position, int i) {
    }
}
