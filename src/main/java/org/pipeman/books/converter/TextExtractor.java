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

import static org.pipeman.books.utils.Utils.Range;

public class TextExtractor {
    public static void main(String[] args) throws IOException {
        Document jsoup = createJsoup(0, 7);
        Element page = jsoup.getElementsByClass("page").get(0);

        System.out.println(getTextOnPage(page.children()));
    }

    public static Document createJsoup(int bookId, int page) throws IOException {
        return Jsoup.parse(new File("book-data/html/" + bookId + "/" + page + ".html"));
    }

    public static List<Range> getPagePositions(int bookId, int pageCount) throws IOException {
        List<Range> pages = new ArrayList<>();

        int position = 0;
        for (int i = 1; i <= pageCount; i++) {
            Document jsoup = createJsoup(bookId, 7);
            Element page = jsoup.getElementsByClass("page").get(0);

            String text = getTextOnPage(page.children());
            int textLength = text.length();
            pages.add(new Range(position, position + textLength));
            position += textLength;
        }
        return pages;
    }

    public static String getText(int bookId) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < BookIndex.INSTANCE.books().get(bookId).pageCount(); i++) {
            sb.append(getTextOnPage(createJsoup(bookId, i).getElementsByClass("page").get(0).children()));
        }
        return sb.toString();
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
