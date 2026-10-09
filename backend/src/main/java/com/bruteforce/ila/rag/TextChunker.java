package com.bruteforce.ila.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Splits raw extracted text into small, coherent chunks suitable for
 * embedding. Splits on sentence boundaries, then groups sentences
 * together until reaching the target chunk size, avoiding mid-sentence
 * cuts.
 */
@Component
public class TextChunker {

    private static final int TARGET_CHUNK_SIZE = 500;

    public List<String> chunk(String rawText) {
        List<String> chunks = new ArrayList<>();

        String cleaned = rawText.replaceAll("\\s+", " ").trim();
        String[] sentences = cleaned.split("(?<=[.!?])\\s+");

        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > TARGET_CHUNK_SIZE && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(sentence).append(" ");
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}