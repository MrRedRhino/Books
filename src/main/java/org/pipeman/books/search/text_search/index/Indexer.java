package org.pipeman.books.search.text_search.index;

import org.pipeman.books.utils.ByteUtils;
import org.pipeman.books.utils.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class Indexer {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\W");

    public static Index indexText(String text) {
        return createIndex(text.toCharArray());
    }

    /*
    Index format:

    int(number of words)
    list(words)
    int(number of wos)
    list(word)
      int(number of wos)
        list(wos)
          int(position)
          String(next word)
          int(i)
     */

    public static void writeIndex(Index index, String filename) throws IOException {
        OutputStream os = Files.newOutputStream(Path.of(filename));

        os.write(ByteUtils.intToBytes(index.words().length));
        for (String word : index.words()) os.write(ByteUtils.stringToBytes(word));

        os.write(ByteUtils.intToBytes(index.wos().size()));
        for (Map.Entry<String, List<Index.WordOccurrence>> e : index.wos().entrySet()) {
            os.write(ByteUtils.stringToBytes(e.getKey()));
            List<Index.WordOccurrence> wos = e.getValue();
            os.write(ByteUtils.intToBytes(wos.size()));

            for (Index.WordOccurrence wo : wos) {
                os.write(ByteUtils.intToBytes(wo.position()));
                os.write(ByteUtils.stringToBytes(wo.nextWord()));
                os.write(ByteUtils.intToBytes(wo.i()));
            }
        }
        os.close();
    }

    public static Index readIndex(String filename) throws IOException {
        BufferedInputStream is = new BufferedInputStream(Files.newInputStream(Path.of(filename)));

        String[] words = new String[ByteUtils.bytesToInt(is.readNBytes(4))];
        for (int i = 0; i < words.length; i++) words[i] = ByteUtils.readString(is);

        int mapLen = ByteUtils.bytesToInt(is.readNBytes(4));
        Map<String, List<Index.WordOccurrence>> index = new HashMap<>(mapLen);
        for (int i = 0; i < mapLen; i++) {
            String key = ByteUtils.readString(is);
            int woCount = ByteUtils.bytesToInt(is.readNBytes(4));
            List<Index.WordOccurrence> wos = new ArrayList<>(woCount);
            for (int j = 0; j < woCount; j++) {
                wos.add(new Index.WordOccurrence(
                        ByteUtils.bytesToInt(is.readNBytes(4)),
                        ByteUtils.readString(is),
                        ByteUtils.bytesToInt(is.readNBytes(4)))
                );
            }
            index.put(key, wos);
        }
        is.close();
        return new Index(index, words);
    }

    private static Index createIndex(char[] data) {
        final List<String> words = new ArrayList<>();
        final Map<String, List<Index.WordOccurrence>> index = new HashMap<>();

        StringBuilder wordBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (SPLIT_PATTERN.matcher(String.valueOf(data[i])).matches()) {
                if (wordBuilder.isEmpty()) continue;
                String word = wordBuilder.toString();

                if (!word.isBlank() && word.length() > 1) {
                    words.add(word);

                    int pos = i - word.length();
                    int nextWordPos = iterateToNextWord(data, i);
                    i = nextWordPos - 1;

                    List<Index.WordOccurrence> list = index.get(word);
                    Index.WordOccurrence o = new Index.WordOccurrence(pos, getNextWord(data, nextWordPos), words.size() - 1);
                    if (list == null) index.put(word, new ArrayList<>(List.of(o)));
                    else list.add(o);
                }

                wordBuilder = new StringBuilder();
            } else wordBuilder.append(data[i]);
        }

        return new Index(index, Utils.toArray(words));
    }

    private static int iterateToNextWord(char[] data, int start) {
        if (start >= data.length) return start;
        char c = data[start];
        while (SPLIT_PATTERN.matcher(String.valueOf(c)).matches()) {
            if (++start >= data.length) return start;
            c = data[start];
        }
        return start;
    }

    private static String getNextWord(char[] data, int position) {
        if (position >= data.length) return "";
        StringBuilder out = new StringBuilder();
        char c = data[position];
        while (!SPLIT_PATTERN.matcher(String.valueOf(c)).matches()) {
            if (++position >= data.length) return out.toString();
            out.append(c);
            c = data[position];
        }

        return out.toString();
    }
}
