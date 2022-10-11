package org.pipeman.books.converter;


import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;
import org.fit.pdfdom.resource.HtmlResource;
import org.fit.pdfdom.resource.HtmlResourceHandler;
import org.pipeman.books.BookIndex;

import java.io.*;

public class Converter {
    public static void convertBook(BookIndex.Book book, File pdfFile) throws IOException {
        ResourceHandler handler = new ResourceHandler(book.id());
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

            new File("book-data/html/" + book.id()).mkdirs();
            Writer output = new PrintWriter("book-data/html/" + book.id() + "/" + i + ".html");
            pdfDomTree.writeText(pdf, output);
            output.close();
        }
        pdf.close();
    }

    public static void main(String[] args) throws IOException {
        for (BookIndex.Book bookEntry : BookIndex.INSTANCE.books().values()) {
            convertBook(bookEntry, new File(""));
        }
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
            String imagePath = bookId + "/" + id + ".jpg";

            Compression.compress(new ByteArrayInputStream(resource.getData()), "book-data/images/" + imagePath);
//            File file = new File("book-data/images/" + imagePath);
//            FileUtils.writeByteArrayToFile(file, resource.getData());

            return "/images/books/" + imagePath;
        }
    }
}
