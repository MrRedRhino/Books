package org.pipeman.books.search.text_search;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pipeman.books.converter.TextExtractor;
import org.pipeman.books.utils.Utils;

import java.io.IOException;
import java.util.List;

public class Highlighter {
    private static final String OPEN_SPAN = "<span style=\"background-color: #FFFF00\">";
    private static final String CLOSE_SPAN = "</span>";
    private static final List<Utils.Range> PAGES = Utils.tryThis(() -> TextExtractor.getPagePositions(0, 8));

    public static String highlight(int pos, int length) throws IOException {
        int page = Utils.binarySearch(PAGES, pos);
        if (page == 0) throw new IllegalArgumentException("Position not found");
        Document jsoup = TextExtractor.createJsoup(0, page + 1);

        int rawOffset = pos - PAGES.get(page).lower();
        int highlightStart = rawOffset;
        length += highlightStart;
        int highlightedLetters = 0;
        int curPos = 0;

        for (Element element : jsoup.getElementsByClass("page").get(0).children()) {
            if (highlightedLetters >= length) break;

            if (element.tagName().equals("div")) {
                String text = element.text();
                int textLength = text.length();
                if (curPos + textLength >= rawOffset) {
                    int toHighlight = Math.min(length - highlightedLetters, textLength);
                    element.text(highlightWord(highlightStart, toHighlight, text));
                    highlightStart = 0;
                    highlightedLetters += toHighlight;
                }
                curPos += textLength;
            }
        }

        return jsoup.html();
    }

    private static String highlightWord(int offset, int len, String word) {
        return word.substring(0, offset) + OPEN_SPAN + word.substring(offset, len) + CLOSE_SPAN + word.substring(len);
    }
}
