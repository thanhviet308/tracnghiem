package com.example.tracnghiem.controller;

import com.example.tracnghiem.dto.subject.*;
import com.example.tracnghiem.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> listSubjects() {
        return ResponseEntity.ok(subjectService.getSubjects());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest request) {
        return ResponseEntity.ok(subjectService.createSubject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<SubjectResponse> updateSubject(@PathVariable Long id, @RequestBody @Valid SubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    @GetMapping("/{subjectId}/chapters")
    public ResponseEntity<List<ChapterResponse>> listChapters(@PathVariable Long subjectId) {
        return ResponseEntity.ok(subjectService.getChapters(subjectId));
    }

    @PostMapping("/chapters")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ChapterResponse> createChapter(@RequestBody @Valid ChapterRequest request) {
        return ResponseEntity.ok(subjectService.createChapter(request));
    }

    @PutMapping("/chapters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ChapterResponse> updateChapter(@PathVariable Long id, @RequestBody @Valid ChapterRequest request) {
        return ResponseEntity.ok(subjectService.updateChapter(id, request));
    }

    @GetMapping("/chapters/{chapterId}/passages")
    public ResponseEntity<List<PassageResponse>> listPassages(@PathVariable Long chapterId) {
        return ResponseEntity.ok(subjectService.getPassages(chapterId));
    }

    @PostMapping("/passages")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PassageResponse> createPassage(@RequestBody @Valid PassageRequest request) {
        return ResponseEntity.ok(subjectService.createPassage(request));
    }

    @PutMapping("/passages/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PassageResponse> updatePassage(@PathVariable Long id, @RequestBody @Valid PassageRequest request) {
        return ResponseEntity.ok(subjectService.updatePassage(id, request));
    }
}

