package org.pipeman.books.ai.impl;

import org.pipeman.books.ai.Answerer;
import org.pipeman.books.ai.Summarizer;

public class RandomImpl {
    public static final Answerer answerer = new Answerer("") {
        @Override
        public String ask(String task, String referenceText) {
            sleep();
            return "your mom";
        }
    };

    public static final Summarizer summarizer = new Summarizer("") {
        @Override
        public String summarize(String text) {
            System.out.println("summarizing");
            sleep();
            return Math.random() * 42 + "";
        }
    };

    private static void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
