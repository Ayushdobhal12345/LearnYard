package com.bruteforce.ila.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Component
public class TextExtractor {

    private final ChatClient chatClient;

    public TextExtractor(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String extractText(String filePath) {
        Path path = Path.of(filePath);
        String lower = filePath.toLowerCase();
        try {
            if (lower.endsWith(".pdf")) {
                return extractFromPdf(path);
            } else if (lower.endsWith(".txt")) {
                return Files.readString(path);
            } else if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return extractFromImage(path, lower);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported file type. Only .pdf, .txt, .png, .jpg and .jpeg are currently supported.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from file: " + e.getMessage(), e);
        }
    }

    private String extractFromPdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractFromImage(Path path, String lowerCasePath) {
        MimeType mimeType = lowerCasePath.endsWith(".png") ? MimeTypeUtils.IMAGE_PNG : MimeTypeUtils.IMAGE_JPEG;
        FileSystemResource resource = new FileSystemResource(path.toFile());

        return chatClient.prompt()
                .user(u -> u.text(
                                "Extract ALL readable text from this image of study notes, exactly as "
                                        + "written, preserving structure like headings and bullet points "
                                        + "where possible. Return only the extracted text, nothing else.")
                        .media(mimeType, resource))
                .call()
                .content();
    }
}