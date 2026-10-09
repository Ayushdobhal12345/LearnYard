package com.bruteforce.ila.material;

import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final TopicRepository topicRepository;
    private final SyllabusUnitRepository unitRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public StudyMaterialService(StudyMaterialRepository studyMaterialRepository,
                                TopicRepository topicRepository,
                                SyllabusUnitRepository unitRepository) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.topicRepository = topicRepository;
        this.unitRepository = unitRepository;
    }

    public StudyMaterial uploadMaterial(MultipartFile file, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Topic with id " + topicId + " not found."));

        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);

            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadPath.resolve(storedFileName);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            StudyMaterial material = new StudyMaterial(
                    file.getOriginalFilename(),
                    targetPath.toString(),
                    topic
            );

            return studyMaterialRepository.save(material);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store uploaded file: " + e.getMessage(),
                    e
            );
        }
    }

    public StudyMaterial uploadMaterialForUnit(MultipartFile file, Long unitId) {
        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);

            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadPath.resolve(storedFileName);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            var unit = unitRepository.findById(unitId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Unit with id " + unitId + " not found."
                    ));

            StudyMaterial material = new StudyMaterial(
                    file.getOriginalFilename(),
                    targetPath.toString(),
                    unit
            );

            return studyMaterialRepository.save(material);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store uploaded file: " + e.getMessage(),
                    e
            );
        }
    }

    public List<StudyMaterial> getMaterialsForUnit(Long unitId) {
        return studyMaterialRepository.findByUnitId(unitId);
    }

    public List<StudyMaterial> getMaterialsForTopic(Long topicId) {
        return studyMaterialRepository.findByTopicId(topicId);
    }

    public void markAsProcessed(Long materialId) {
        StudyMaterial material = studyMaterialRepository.findById(materialId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Material with id " + materialId + " not found."
                ));

        material.setIsProcessed(true);
        studyMaterialRepository.save(material);
    }
}