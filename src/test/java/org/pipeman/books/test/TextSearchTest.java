package org.pipeman.books.test;

import org.pipeman.books.Main;
import org.pipeman.books.search.text_search.Sorting;
import org.pipeman.books.search.text_search.TextSearch;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import java.util.List;

public class TextSearchTest {
    static TextSearch SEARCH = new TextSearch();
    static JTextField textField = new JTextField();
    static DefaultListModel<String> list = new DefaultListModel<>();

    public static void actionPerformed(CaretEvent event) {
        String query = ((JTextField) event.getSource()).getText();
        long start = System.nanoTime();
        List<TextSearch.SearchResult> results = SEARCH.search(query, 3, Sorting.LOCATION);

        list.clear();

        System.out.println("Search took: " + (System.nanoTime() - start) / 1_000_000 + "ms");
        for (TextSearch.SearchResult result : results) addElement(result);
    }

    private static void addElement(TextSearch.SearchResult r) {
        StringBuilder out = new StringBuilder();
        TextSearch.Highlight highlight = r.previewHighlight();
        int start = highlight.start();
        out.append(r.preview(), 0, start).append("**");
        int end = Math.min(r.preview().length(), start + highlight.length());
        out.append(r.preview(), start, end).append("**");
        out.append(r.preview(), end, r.preview().length());

        list.addElement(out.toString());
    }

    public static void main(String[] args) {
        new Thread(() -> SEARCH.search("", 3, Sorting.LOCATION)).start();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField f = new JTextField();
        textField = f;

        f.setBounds(130, 60, 500, 40);
        f.addCaretListener(TextSearchTest::actionPerformed);
        frame.add(f);
        frame.setSize(400, 400);
        frame.setLayout(null);
        frame.setVisible(true);

        JList<String> jList = new JList<>(list);
        jList.setVisible(true);
        jList.setBounds(130, 100, 1000, 1000);
        frame.add(jList);
    }
}
