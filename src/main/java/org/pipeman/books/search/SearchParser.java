package org.pipeman.books.search;

import org.pipeman.books.BookIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchParser {
    private final PipeComplete completer = new PipeComplete();

    public SplitResult tidyUpQuery(String query) {
        List<Integer> numbers = new ArrayList<>();
        StringBuilder curNumber = new StringBuilder();
        StringBuilder curString = new StringBuilder();
        boolean lastCharWasNum = false;

        for (char c : query.replace(".", "").toCharArray()) {
            if (Character.isDigit(c)) {
                curNumber.append(c);
                lastCharWasNum = true;
            } else {
                parseAndAdd(curNumber.toString(), numbers);
                curNumber = new StringBuilder();
                if (c != ' ' || !lastCharWasNum) curString.append(c);
                lastCharWasNum = false;
            }
        }
        parseAndAdd(curNumber.toString().strip(), numbers);
        Collections.sort(numbers);
        return new SplitResult(numbers, curString.toString().strip());
    }

    public List<CompletionResult> getCompletions(String query) {
        SplitResult splitResult = tidyUpQuery(query);
        List<CompletionResult> out = new ArrayList<>();

        for (BookIndex.Book book : completer.getCompletionsSorted(splitResult.rest())) {
            List<Integer> numbers = splitResult.numbers();
            if (numbers.size() == 0) out.add(new CompletionResult(book, 1));
            else for (Integer number : numbers) out.add(new CompletionResult(book, number));
        }
        return out;
    }

    private void parseAndAdd(String s, List<Integer> list) {
        try {
            list.add(Integer.parseInt(s));
        } catch (Exception ignored) {
        }
    }

    public record CompletionResult(BookIndex.Book book, int page) {
    }

    private record SplitResult(List<Integer> numbers, String rest) {
    }
}
