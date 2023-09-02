package org.pipeman.books.ai;

public abstract class Answerer {
    protected final String token;

    public Answerer(String token) {
        this.token = token;
    }

    public abstract String ask(String task, String referenceText);
}
