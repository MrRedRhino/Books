package org.pipeman.books.ai;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.pipeman.books.ai.impl.OpenAIAnswerer;
import org.pipeman.books.ai.impl.OpenAISummarizer;
import org.pipeman.books.ai.impl.RandomImpl;
import org.pipeman.books.converter.TextExtractor;
import org.pipeman.books.utils.ByteUtils;
import org.pipeman.books.utils.Utils;
import org.pipeman.books.utils.Utils.Range;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AI {
    private static final String openAIKey = "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";

    private static final Summarizer SUMMARIZER = RandomImpl.summarizer;
    private static final Answerer ANSWERER = new OpenAIAnswerer(openAIKey);

    private static final DB summaries;

    static {
        Options options = new Options();
        options.cacheSize(4 * 1024 * 1024);
        summaries = Utils.tryThis(() -> Iq80DBFactory.factory.open(new File("summaries"), options));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Utils.tryThis(summaries::close)));
    }

    public static synchronized String getSummary(int bookId, int page) {
        String key = bookId + " " + page;

        synchronized (key) {
            byte[] keyBytes = key.getBytes();
            byte[] bytes = summaries.get(keyBytes);

            if (bytes == null) {
                String summary = SUMMARIZER.summarize(TextExtractor.getText(bookId, page));
                summaries.put(keyBytes, summary.getBytes());
                return summary;
            } else {
                return new String(bytes);
            }
        }
    }

    public static String getAnswer(String question, int bookId, Range pages) {
        StringBuilder text = new StringBuilder();
        for (int page : pages) {
            text.append(TextExtractor.getText(bookId, page)).append(" ");
        }

        return ANSWERER.ask(question, text.toString());
    }
}
