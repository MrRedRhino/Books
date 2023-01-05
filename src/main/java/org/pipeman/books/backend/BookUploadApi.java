package org.pipeman.books.backend;

import io.javalin.http.Context;
import org.pipeman.books.utils.CsvWriter;
import org.pipeman.books.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BookUploadApi {
    private static final int MAX_SIZE = 1_073_741_824;

    static {
        new File("uploads").mkdir();
    }

    public static Context handleUpload(Context ctx) throws IOException {
        String subject = ctx.queryParam("subject");
        if (subject == null) return ctx.status(400).json(Map.of("error", "Query param 'subject' missing"));
        String title = ctx.queryParam("title");
        if (title == null) return ctx.status(400).json(Map.of("error", "Query param 'title' missing"));

        File file = new File("uploads", System.nanoTime() + ".pdf");

        byte[] buffer = new byte[8192];
        int transferred = 0;
        try (OutputStream out = new FileOutputStream(file);
             InputStream body = ctx.bodyInputStream()) {
            int read;
            while ((read = body.read(buffer)) >= 0) {
                if (transferred > MAX_SIZE) {
                    file.delete();
                    return ctx.status(400).json(Map.of("error", "Upload too large"));
                }

                out.write(buffer, 0, read);
                transferred += read;
            }
        } catch (IOException ignored) {
            // delete file
            file.delete();
            return ctx.status(500).json(Map.of("error", "Upload failed"));
        }

        try (OutputStream os = Files.newOutputStream(Path.of("uploads", "uploads.csv"),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            new CsvWriter(os).writeRow(Utils.substr(subject, 150), Utils.substr(title, 150), file.getName());
        }
        return ctx;
    }
}
