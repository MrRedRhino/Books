package org.pipeman.books.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.System.in;

public class TerminalUtil {
    public static String readLine() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String line = reader.readLine();
            if (line.equalsIgnoreCase("q")) System.exit(0);
            return line;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int readInt() {
        while (true) {
            System.out.print("> ");
            String l = readLine();
            if (l == null) continue;

            try {
                return Integer.parseInt(l);
            } catch (Exception ignored) {
            }
        }
    }
}
