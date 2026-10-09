package com.bruteforce.ila.rag;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.bruteforce.ila.syllabus.TopicRepository;
import org.springframework.stereotype.Service;

@Service
public class RagIngestionService {

    private final TextExtractor textExtractor;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final Index pineconeIndex;
    private final TopicRepository topicRepository;

    public RagIngestionService(TextExtractor textExtractor, TextChunker textChunker,
                               EmbeddingService embeddingService, Index pineconeIndex,
                               TopicRepository topicRepository) {
        this.textExtractor = textExtractor;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
        this.pineconeIndex = pineconeIndex;
        this.topicRepository = topicRepository;
    }

    /**
     * Full pipeline: extract text from the file, chunk it, embed each chunk,
     * and upsert every chunk's vector into Pinecone with metadata
     * (topicId, materialId, and the original chunk text itself).
     */
    public int processMaterial(String filePath, Long topicId, Long materialId) {
        return ingest(filePath, "topicId", topicId, materialId);
    }

    /** Notes uploaded for a whole unit: stored under the unit, found by every topic in it. */
    public int processMaterialForUnit(String filePath, Long unitId, Long materialId) {
        return ingest(filePath, "unitId", unitId, materialId);
    }

    private int ingest(String filePath, String ownerKey, Long ownerId, Long materialId) {
        String rawText = textExtractor.extractText(filePath);
        List<String> chunks = textChunker.chunk(rawText);

        List<VectorWithUnsignedIndices> vectors = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            float[] embedding = embeddingService.embed(chunkText);

            List<Float> vectorValues = new ArrayList<>();
            for (float v : embedding) {
                vectorValues.add(v);
            }

            Struct metadata = Struct.newBuilder()
                    .putFields(ownerKey, Value.newBuilder().setStringValue(String.valueOf(ownerId)).build())
                    .putFields("materialId", Value.newBuilder().setStringValue(String.valueOf(materialId)).build())
                    .putFields("text", Value.newBuilder().setStringValue(chunkText).build())
                    .build();

            String vectorId = "material-" + materialId + "-chunk-" + i + "-" + UUID.randomUUID();

            VectorWithUnsignedIndices vector = new VectorWithUnsignedIndices(vectorId, vectorValues);
            vector.setMetadata(metadata);
            vectors.add(vector);
        }

        pineconeIndex.upsert(vectors, "");
        return chunks.size();
    }

    /**
     * Semantic search: embeds the query, asks Pinecone for the most similar
     * stored chunks, optionally scoped to one topic, and returns their
     * original text - ready to hand to the LLM as grounding context.
     */
    public List<String> searchRelevantChunks(String query, Long topicId, int topK) {
        float[] queryEmbedding = embeddingService.embed(query);
        List<Float> queryVector = new ArrayList<>();
        for (float v : queryEmbedding) {
            queryVector.add(v);
        }

        Struct filter = null;
        if (topicId != null) {
            Struct topicCondition = Struct.newBuilder()
                    .putFields("topicId", Value.newBuilder().setStringValue(String.valueOf(topicId)).build())
                    .build();

            Long unitId = topicRepository.findUnitIdByTopicId(topicId).orElse(null);

            if (unitId == null) {
                filter = topicCondition;
            } else {
                // notes saved for this topic OR for the unit it belongs to
                Struct unitCondition = Struct.newBuilder()
                        .putFields("unitId", Value.newBuilder().setStringValue(String.valueOf(unitId)).build())
                        .build();
                ListValue either = ListValue.newBuilder()
                        .addValues(Value.newBuilder().setStructValue(topicCondition).build())
                        .addValues(Value.newBuilder().setStructValue(unitCondition).build())
                        .build();
                filter = Struct.newBuilder()
                        .putFields("$or", Value.newBuilder().setListValue(either).build())
                        .build();
            }
        }

        QueryResponseWithUnsignedIndices response = pineconeIndex.query(
                topK, queryVector, null, null, null, null, filter, true, true);

        List<String> results = new ArrayList<>();
        for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
            Value textValue = match.getMetadata().getFieldsMap().get("text");
            if (textValue != null) {
                results.add(textValue.getStringValue());
            }
        }
        return results;
    }
}