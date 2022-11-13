package org.pipeman.books.test;

import org.pipeman.books.search.text_search.TextSearch;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import java.util.List;

public class TextSearchTest {
    static JTextField textField = new JTextField();
    static DefaultListModel<String> list = new DefaultListModel<>();

    public static void actionPerformed(CaretEvent event) {
        String query = ((JTextField) event.getSource()).getText();
        long start = System.nanoTime();
        List<TextSearch.SearchResult> results = TextSearch.INSTANCE.search(query);

        list.clear();

        System.out.println("Search took: " + (System.nanoTime() - start) / 1_000_000 + "ms");
        for (TextSearch.SearchResult wo : results) addElement(wo);
    }

    private static void addElement(TextSearch.SearchResult r) {
        StringBuilder out = new StringBuilder();
        TextSearch.Highlight ph = r.previewHighlight();
        out.append(r.preview(), 0, ph.start()).append("**");
        out.append(r.preview(), ph.start(), ph.start() + ph.length()).append("**");
        out.append(r.preview(), ph.start() + ph.length(), r.preview().length());

        list.addElement(out.toString());
    }

    public static void main(String[] args) {
        TextSearch.INSTANCE.init();
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
