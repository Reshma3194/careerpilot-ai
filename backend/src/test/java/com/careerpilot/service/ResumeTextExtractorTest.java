package com.careerpilot.service;

import com.careerpilot.util.ResumeTextExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeTextExtractorTest {

    private final ResumeTextExtractor extractor = new ResumeTextExtractor();

    @Test
    void readsPlainText() {
        MockMultipartFile file = new MockMultipartFile("file", "cv.txt", "text/plain", "  Hello Java  ".getBytes());
        assertEquals("Hello Java", extractor.extract(file));
    }

    @Test
    void readsDocx() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("Spring Boot developer");
            doc.write(out);
        }
        MockMultipartFile file = new MockMultipartFile("file", "cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", out.toByteArray());
        assertTrue(extractor.extract(file).contains("Spring Boot developer"));
    }

    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "cv.exe", "application/octet-stream", "x".getBytes());
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(file));
    }

    @Test
    void rejectsEmptyText() {
        MockMultipartFile file = new MockMultipartFile("file", "cv.txt", "text/plain", "   ".getBytes());
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(file));
    }
}
