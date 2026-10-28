package com.careerpilot.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Extracts plain text from uploaded resumes (PDF, DOCX, TXT). */
@Component
public class ResumeTextExtractor {

    public String extract(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            byte[] bytes = file.getBytes();
            String text;
            if (name.endsWith(".pdf")) {
                text = fromPdf(bytes);
            } else if (name.endsWith(".docx")) {
                text = fromDocx(bytes);
            } else if (name.endsWith(".txt")) {
                text = new String(bytes, StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("Unsupported file type. Upload a PDF, DOCX or TXT resume.");
            }

            String cleaned = text == null ? "" : text.replace("\u0000", "").trim();
            if (cleaned.isEmpty()) {
                throw new IllegalArgumentException(
                        "Could not read any text from this file. If it is a scanned PDF, upload a text-based version.");
            }
            return cleaned;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read the uploaded file. It may be corrupted or password protected.");
        }
    }

    private String fromPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String fromDocx(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
