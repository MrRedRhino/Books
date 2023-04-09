package org.pipeman.books.converter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pipeman.books.BookIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextExtractor {
    public static Document createJsoup(int bookId, int page) throws IOException {
        return Jsoup.parse(new File("book-data/html/" + bookId + "/" + page + ".html"));
    }

    public static List<String> getTexts(int bookId) throws IOException {
        List<String> texts = new ArrayList<>();
        for (int i = 1; i < BookIndex.INSTANCE.books().get(bookId).pageCount(); i++) {
            texts.add(getTextOnPage(createJsoup(bookId, i).getElementsByClass("page").get(0).children()));
        }
        return texts;
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
