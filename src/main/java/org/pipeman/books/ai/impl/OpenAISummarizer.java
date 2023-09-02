package org.pipeman.books.ai.impl;

import org.pipeman.books.ai.Summarizer;

public class OpenAISummarizer extends Summarizer {

    public OpenAISummarizer(String token) {
        super(token);
    }

    @Override
    public String summarize(String text) {
        String task = "%s\n\nExtrahiere die wichtigsten Inhalte. Max. 100 Wörter".formatted(text);

        return OpenAI.getCompletion(task, token).trim();
    }
}
