package org.pipeman.books.utils;

import java.io.IOException;
import java.io.OutputStream;

public class CsvWriter {
    // row: zeile
    // column: spalte
    private final OutputStream os;

    public CsvWriter(OutputStream os) {
        this.os = os;
    }

    public CsvWriter writeRow(String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            os.write(quote(values[i]).getBytes());
            if (i < values.length - 1) os.write(',');
        }
        os.write('\n');
        return this;
    }

    private String quote(String s) {
        return s.contains("\"") || s.contains(",") ? '"' + s.replace("\"", "\"\"") + '"' : s;
    }
}
