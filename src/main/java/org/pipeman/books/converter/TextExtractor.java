package org.pipeman.books.converter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pipeman.books.BookIndex;

import java.util.ArrayList;
import java.util.List;

public class TextExtractor {
    public static Document createJsoup(int bookId, int page) {
        return Jsoup.parse(BookIndex.INSTANCE.getHtml(bookId, page));
    }

    public static List<String> getTexts(int bookId) {
        List<String> texts = new ArrayList<>();
        int pageCount = BookIndex.INSTANCE.books().get(bookId).pageCount();
        for (int i = 1; i < pageCount; i++) {
            texts.add(getText(bookId, i));
        }
        return texts;
    }

    public static String getText(int bookId, int page) {
        return getTextOnPage(createJsoup(bookId, page).getElementsByClass("page").get(0).children());
    }

    private static String getTextOnPage(Elements elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            if (element.tagName().equals("div")) {
                String text = element.text();
                String withoutHyphen = stripLastHyphen(text);
                sb.append(withoutHyphen).append(withoutHyphen.equals(text) ? " " : "");
            }
        }
        return sb.toString();
    }

    private static String stripLastHyphen(String input) {
        return input.endsWith("-") ? input.substring(0, input.length() - 1) : input;
    }
}
