package com.example.tracnghiem.controller;

import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.group.StudentGroupRequest;
import com.example.tracnghiem.dto.group.StudentGroupResponse;
import com.example.tracnghiem.dto.group.StudentGroupSubjectRequest;
import com.example.tracnghiem.dto.group.StudentGroupSubjectResponse;
import com.example.tracnghiem.service.StudentGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-groups")
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    public StudentGroupController(StudentGroupService studentGroupService) {
        this.studentGroupService = studentGroupService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','SUPERVISOR')")
    public ResponseEntity<List<StudentGroupResponse>> listGroups() {
        return ResponseEntity.ok(studentGroupService.listGroups());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentGroupResponse> createGroup(@RequestBody @Valid StudentGroupRequest request) {
        return ResponseEntity.ok(studentGroupService.createGroup(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentGroupResponse> updateGroup(@PathVariable Long id,
            @RequestBody @Valid StudentGroupRequest request) {
        return ResponseEntity.ok(studentGroupService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        studentGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // --- Assign students to group ---

    @PutMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignStudents(@PathVariable Long id, @RequestBody List<Long> studentIds) {
        studentGroupService.assignStudents(id, studentIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','SUPERVISOR')")
    public ResponseEntity<List<com.example.tracnghiem.dto.user.UserResponse>> getStudentsInGroup(
            @PathVariable Long id) {
        return ResponseEntity.ok(studentGroupService.getStudentsInGroup(id));
    }

    // --- Assign subject to group with teacher ---

    @GetMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<StudentGroupSubjectResponse>> listAssignments() {
        return ResponseEntity.ok(studentGroupService.listAssignments());
    }

    @GetMapping("/subjects/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentGroupSubjectResponse>> getMyAssignments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(studentGroupService.getAssignmentsByTeacher(user.getId()));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentGroupSubjectResponse> createAssignment(
            @RequestBody @Valid StudentGroupSubjectRequest request) {
        return ResponseEntity.ok(studentGroupService.createAssignment(request));
    }

    @PutMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentGroupSubjectResponse> updateAssignment(
            @RequestParam Long groupId,
            @RequestParam Long subjectId,
            @RequestBody @Valid StudentGroupSubjectRequest request) {
        return ResponseEntity.ok(studentGroupService.updateAssignment(groupId, subjectId, request));
    }

    @DeleteMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@RequestParam Long groupId, @RequestParam Long subjectId) {
        studentGroupService.deleteAssignment(groupId, subjectId);
        return ResponseEntity.noContent().build();
    }
}
