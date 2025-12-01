package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.ExamAttempt;
import com.example.tracnghiem.domain.exam.ExamViolation;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.ViolationRequest;
import com.example.tracnghiem.dto.exam.ViolationResponse;
import com.example.tracnghiem.exception.ForbiddenException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ExamAttemptRepository;
import com.example.tracnghiem.repository.ExamViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ViolationService {

    private final ExamViolationRepository violationRepository;
    private final ExamAttemptRepository attemptRepository;

    public ViolationService(ExamViolationRepository violationRepository,
                           ExamAttemptRepository attemptRepository) {
        this.violationRepository = violationRepository;
        this.attemptRepository = attemptRepository;
    }

    public void reportViolation(Long attemptId, User student, ViolationRequest request) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        
        // Verify this is the student's own attempt
        if (!Objects.equals(attempt.getStudent().getId(), student.getId())) {
            throw new ForbiddenException("Not your attempt");
        }
        
        // Check if violation type already exists for this attempt
        var existing = violationRepository.findByAttempt_IdAndViolationType(attemptId, request.violationType());
        
        if (existing.isPresent()) {
            // Increment count
            ExamViolation violation = existing.get();
            violation.setViolationCount(violation.getViolationCount() + 1);
            violation.setLastOccurredAt(Instant.now());
            violationRepository.save(violation);
        } else {
            // Create new violation record
            ExamViolation violation = ExamViolation.builder()
                    .attempt(attempt)
                    .violationType(request.violationType())
                    .violationCount(1)
                    .lastOccurredAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();
            violationRepository.save(violation);
        }
    }

    public List<ViolationResponse> getViolationsForExam(Long examInstanceId) {
        return violationRepository.findByAttempt_ExamInstance_Id(examInstanceId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ViolationResponse> getViolationsForAttempt(Long attemptId) {
        return violationRepository.findByAttempt_Id(attemptId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ViolationResponse toResponse(ExamViolation violation) {
        return new ViolationResponse(
                violation.getId(),
                violation.getAttempt().getId(),
                violation.getAttempt().getStudent().getId(),
                violation.getAttempt().getStudent().getFullName(),
                violation.getViolationType(),
                violation.getViolationCount(),
                violation.getLastOccurredAt()
        );
    }
}

