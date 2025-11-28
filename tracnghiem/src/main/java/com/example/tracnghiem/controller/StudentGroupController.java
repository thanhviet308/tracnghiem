package com.example.tracnghiem.controller;

import com.example.tracnghiem.dto.group.StudentGroupRequest;
import com.example.tracnghiem.dto.group.StudentGroupResponse;
import com.example.tracnghiem.dto.group.StudentGroupSubjectRequest;
import com.example.tracnghiem.dto.group.StudentGroupSubjectResponse;
import com.example.tracnghiem.service.StudentGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-groups")
@PreAuthorize("hasRole('ADMIN')")
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    public StudentGroupController(StudentGroupService studentGroupService) {
        this.studentGroupService = studentGroupService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<StudentGroupResponse>> listGroups() {
        return ResponseEntity.ok(studentGroupService.listGroups());
    }

    @PostMapping
    public ResponseEntity<StudentGroupResponse> createGroup(@RequestBody @Valid StudentGroupRequest request) {
        return ResponseEntity.ok(studentGroupService.createGroup(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentGroupResponse> updateGroup(@PathVariable Long id,
                                                            @RequestBody @Valid StudentGroupRequest request) {
        return ResponseEntity.ok(studentGroupService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        studentGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // --- Assign subject to group with teacher ---

    @GetMapping("/subjects")
    public ResponseEntity<List<StudentGroupSubjectResponse>> listAssignments() {
        return ResponseEntity.ok(studentGroupService.listAssignments());
    }

    @PostMapping("/subjects")
    public ResponseEntity<StudentGroupSubjectResponse> createAssignment(
            @RequestBody @Valid StudentGroupSubjectRequest request) {
        return ResponseEntity.ok(studentGroupService.createAssignment(request));
    }

    @DeleteMapping("/subjects")
    public ResponseEntity<Void> deleteAssignment(@RequestParam Long groupId, @RequestParam Long subjectId) {
        studentGroupService.deleteAssignment(groupId, subjectId);
        return ResponseEntity.noContent().build();
    }
}


