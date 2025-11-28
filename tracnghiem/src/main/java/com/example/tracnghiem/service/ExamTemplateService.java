package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.ExamStructure;
import com.example.tracnghiem.domain.exam.ExamTemplate;
import com.example.tracnghiem.domain.subject.Chapter;
import com.example.tracnghiem.domain.subject.Subject;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.CreateExamTemplateRequest;
import com.example.tracnghiem.dto.exam.ExamStructureRequest;
import com.example.tracnghiem.dto.exam.ExamTemplateResponse;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ChapterRepository;
import com.example.tracnghiem.repository.ExamStructureRepository;
import com.example.tracnghiem.repository.ExamTemplateRepository;
import com.example.tracnghiem.repository.QuestionRepository;
import com.example.tracnghiem.repository.SubjectRepository;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExamTemplateService {

    private final ExamTemplateRepository templateRepository;
    private final ExamStructureRepository structureRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ExamTemplateService(ExamTemplateRepository templateRepository,
                               ExamStructureRepository structureRepository,
                               SubjectRepository subjectRepository,
                               ChapterRepository chapterRepository,
                               QuestionRepository questionRepository,
                               UserRepository userRepository) {
        this.templateRepository = templateRepository;
        this.structureRepository = structureRepository;
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public ExamTemplateResponse createTemplate(CreateExamTemplateRequest request, Long creatorId) {
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        validateStructure(request);

        ExamTemplate template = ExamTemplate.builder()
                .subject(subject)
                .name(request.name())
                .totalQuestions(request.totalQuestions())
                .durationMinutes(request.durationMinutes())
                .createdBy(creator)
                .structures(new ArrayList<>()) // Initialize empty list to avoid NullPointerException
                .build();

        // Only process structures if provided and not empty
        if (request.structures() != null && !request.structures().isEmpty()) {
            Map<Long, Chapter> chapterCache = new HashMap<>();
            request.structures().forEach(structureRequest -> {
                Chapter chapter = chapterCache.computeIfAbsent(structureRequest.chapterId(), id ->
                        chapterRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + id)));
                long available = questionRepository.countByChapter_IdAndActiveTrue(chapter.getId());
                if (available < structureRequest.numQuestion()) {
                    throw new BadRequestException("Not enough questions in chapter " + chapter.getName());
                }
                ExamStructure structure = ExamStructure.builder()
                        .chapter(chapter)
                        .template(template)
                        .numQuestion(structureRequest.numQuestion())
                        .build();
                template.getStructures().add(structure);
            });
        }

        ExamTemplate savedTemplate = templateRepository.save(template);
        templateRepository.flush();
        
        // Fetch again with relationships to avoid lazy loading issues
        // The findByIdWithRelations query will eagerly fetch subject and structures with chapters
        ExamTemplate templateWithRelations = templateRepository.findByIdWithRelations(savedTemplate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam template not found after creation"));
        
        return toResponse(templateWithRelations);
    }

    public List<ExamTemplateResponse> getTemplatesBySubject(Long subjectId) {
        return templateRepository.findBySubject_Id(subjectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamTemplateResponse> getAllTemplates() {
        List<ExamTemplate> templates = templateRepository.findAllWithSubject();
        // Fetch structures for all templates to avoid N+1
        List<Long> templateIds = templates.stream().map(ExamTemplate::getId).toList();
        if (!templateIds.isEmpty()) {
            List<ExamStructure> allStructures = structureRepository.findByTemplate_IdIn(templateIds);
            // Group structures by template ID - use template from structures but match by ID
            Map<Long, List<ExamStructure>> structuresByTemplate = new HashMap<>();
            for (ExamStructure structure : allStructures) {
                // Get template ID from the structure's template (lazy loaded but should be available in transaction)
                Long templateId = structure.getTemplate().getId();
                structuresByTemplate.computeIfAbsent(templateId, k -> new ArrayList<>()).add(structure);
            }
            // Set structures for each template
            for (ExamTemplate template : templates) {
                List<ExamStructure> structures = structuresByTemplate.getOrDefault(template.getId(), new ArrayList<>());
                template.getStructures().clear();
                template.getStructures().addAll(structures);
            }
        }
        return templates.stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamTemplateResponse getTemplate(Long id) {
        return toResponse(getTemplateEntity(id));
    }

    public ExamTemplate getTemplateEntity(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam template not found"));
    }

    private void validateStructure(CreateExamTemplateRequest request) {
        // Allow empty structures - can be added later
        if (request.structures() == null) {
            return; // Allow null structures
        }
        if (request.structures().isEmpty()) {
            return; // Allow empty structures - will be added later via structure drawer
        }
        int total = request.structures().stream().mapToInt(ExamStructureRequest::numQuestion).sum();
        if (total != request.totalQuestions()) {
            throw new BadRequestException("Sum of chapter questions must equal total questions");
        }
    }

    private ExamTemplateResponse toResponse(ExamTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        
        // Get structures - handle null or empty safely
        List<ExamTemplateResponse.ExamStructurePayload> structures = List.of();
        if (template.getStructures() != null && !template.getStructures().isEmpty()) {
            structures = template.getStructures().stream()
                    .map(struct -> {
                        if (struct.getChapter() == null) {
                            throw new IllegalStateException(
                                    String.format("Chapter is null for structure ID: %d in template ID: %d", 
                                            struct.getId(), template.getId()));
                        }
                        return new ExamTemplateResponse.ExamStructurePayload(
                                struct.getId(),
                                struct.getChapter().getId(),
                                struct.getNumQuestion()
                        );
                    })
                    .toList();
        }
        
        // Get subject ID - must not be null
        if (template.getSubject() == null) {
            throw new IllegalStateException(
                    String.format("Subject is null for template ID: %d", template.getId()));
        }
        Long subjectId = template.getSubject().getId();
        
        return new ExamTemplateResponse(
                template.getId(),
                subjectId,
                template.getName(),
                template.getTotalQuestions(),
                template.getDurationMinutes(),
                structures
        );
    }
}

