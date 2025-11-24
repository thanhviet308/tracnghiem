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
import com.example.tracnghiem.repository.ExamTemplateRepository;
import com.example.tracnghiem.repository.QuestionRepository;
import com.example.tracnghiem.repository.SubjectRepository;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExamTemplateService {

    private final ExamTemplateRepository templateRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ExamTemplateService(ExamTemplateRepository templateRepository,
                               SubjectRepository subjectRepository,
                               ChapterRepository chapterRepository,
                               QuestionRepository questionRepository,
                               UserRepository userRepository) {
        this.templateRepository = templateRepository;
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
                .build();

        template.getStructures().clear();
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

        return toResponse(templateRepository.save(template));
    }

    public List<ExamTemplateResponse> getTemplatesBySubject(Long subjectId) {
        return templateRepository.findBySubject_Id(subjectId).stream()
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
        if (request.structures() == null || request.structures().isEmpty()) {
            throw new BadRequestException("Template requires chapter structure");
        }
        int total = request.structures().stream().mapToInt(ExamStructureRequest::numQuestion).sum();
        if (total != request.totalQuestions()) {
            throw new BadRequestException("Sum of chapter questions must equal total questions");
        }
    }

    private ExamTemplateResponse toResponse(ExamTemplate template) {
        List<ExamTemplateResponse.ExamStructurePayload> structures = template.getStructures().stream()
                .map(struct -> new ExamTemplateResponse.ExamStructurePayload(
                        struct.getId(),
                        struct.getChapter().getId(),
                        struct.getNumQuestion()
                )).toList();
        return new ExamTemplateResponse(
                template.getId(),
                template.getSubject().getId(),
                template.getName(),
                template.getTotalQuestions(),
                template.getDurationMinutes(),
                structures
        );
    }
}

