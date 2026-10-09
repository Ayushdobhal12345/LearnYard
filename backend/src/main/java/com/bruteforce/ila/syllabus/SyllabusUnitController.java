package com.bruteforce.ila.syllabus;

import com.bruteforce.ila.curriculum.CascadeDeletionService;
import com.bruteforce.ila.syllabus.dto.UnitRequest;
import com.bruteforce.ila.syllabus.dto.UnitResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/units")
public class SyllabusUnitController {

    private final SyllabusUnitService unitService;
    private final com.bruteforce.ila.curriculum.CascadeDeletionService cascadeDeletionService;

    public SyllabusUnitController(SyllabusUnitService unitService, CascadeDeletionService cascadeDeletionService) {
        this.unitService = unitService;
        this.cascadeDeletionService = cascadeDeletionService;
    }

    @PostMapping
    public ResponseEntity<UnitResponse> createUnit(@Valid @RequestBody UnitRequest request) {
        SyllabusUnit unit = unitService.createUnit(
                request.getName(), request.getDescription(),
                request.getUnitOrder(), request.getSubjectId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(unit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        cascadeDeletionService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-subject/{subjectId}")
    public ResponseEntity<List<UnitResponse>> getUnitsBySubject(@PathVariable Long subjectId) {
        List<UnitResponse> units = unitService.getUnitsBySubject(subjectId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(units);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitResponse> getUnitById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(unitService.getUnitById(id)));
    }

    private UnitResponse toResponse(SyllabusUnit unit) {
        return new UnitResponse(
                unit.getId(), unit.getName(), unit.getDescription(), unit.getUnitOrder(),
                unit.getSubject().getId(), unit.getSubject().getName());
    }
}