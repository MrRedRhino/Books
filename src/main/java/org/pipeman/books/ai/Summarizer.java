package org.pipeman.books.ai;

public abstract class Summarizer {
    protected final String token;

    public Summarizer(String token) {
        this.token = token;
    }

    public abstract String summarize(String text);
}
