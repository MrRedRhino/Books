package org.pipeman.books.ai.impl;

import org.pipeman.books.ai.Answerer;

public class OpenAIAnswerer extends Answerer {
    public OpenAIAnswerer(String token) {
        super(token);
    }

    @Override
    public String ask(String task, String referenceText) {
        String prompt = "%s\n%s".formatted(referenceText, task);

        return OpenAI.getCompletion(prompt, token);
    }
}
