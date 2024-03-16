package org.pipeman.books.converter;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;
import org.fit.pdfdom.resource.HtmlResource;
import org.fit.pdfdom.resource.HtmlResourceHandler;
import org.pipeman.books.utils.TerminalUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Converter {
    private static final Pattern IMG_PATTERN = Pattern.compile("<img src=\"(.*(?=\\.jpg)).*>");

    public static void convertBook(int bookId, File pdfFile) throws IOException {
        ResourceHandler handler = new ResourceHandler(bookId);
        PDDocument pdf = PDDocument.load(pdfFile);

        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setImageHandler(handler);

        int pageCount = pdf.getNumberOfPages();

        PDFDomTree pdfDomTree = new PDFDomTree(config);
        for (int i = 1; i <= pageCount; i++) {
            System.out.println(i + "/" + pageCount);

            handler.setId(i);
            pdfDomTree.setStartPage(i);
            pdfDomTree.setEndPage(i);

            new File("book-data/html/" + bookId).mkdirs();
            StringWriter htmlWriter = new StringWriter();
            pdfDomTree.writeText(pdf, htmlWriter);
            String html = htmlWriter.toString();

            String transformedHtml = IMG_PATTERN.matcher(html).replaceFirst(result -> {
                String match = result.group();
                String url = result.group(1);
                String firstTag = match.replace(url, url + "-thumbnail");
                String secondTag = match
                        .replace("style=\"", "style=\"z-index:100;opacity:0;")
                        .replace("<img", "<img onload=\"this.style.opacity='1'\"");
                return firstTag + secondTag;
            });

            Files.writeString(Path.of("book-data/html/" + bookId + "/" + i + ".html"), transformedHtml);
        }
        pdf.close();
    }

    public static void main(String[] args) throws IOException {
        int bookId;
        File pdf;
        float compressionQuality;

        System.out.print("Enter the id for the new book (must be an integer)");
        bookId = TerminalUtil.readInt();

        while (true) {
            System.out.print("Enter the path of the new pdf file > ");
            File f = new File(TerminalUtil.readLine());
            if (f.exists()) {
                pdf = f;
                break;
            }
            System.out.println("File does not exist");
        }

        while (true) {
            try {
                System.out.print("Enter the compression quality (lower values produce worse results, range 0-1) > ");
                float f = Float.parseFloat(TerminalUtil.readLine());
                if (f < 0 || f > 1) System.out.println("Value has to be between 0 and 1");
                else {
                    compressionQuality = f;
                    break;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Compression.compressionQuality = compressionQuality;
        Converter.convertBook(bookId, pdf);
    }

    private static class ResourceHandler implements HtmlResourceHandler {
        private final int bookId;
        private int id = 0;

        public ResourceHandler(int bookId) {
            this.bookId = bookId;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String handleResource(HtmlResource resource) throws IOException {
            String imagePath = bookId + "/" + id;

            Compression.compress(new ByteArrayInputStream(resource.getData()), "book-data/images/" + imagePath);

            return "/images/books/" + imagePath + ".jpg";
        }
    }
}
