package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.*;
import com.example.tracnghiem.domain.group.StudentGroup;
import com.example.tracnghiem.domain.question.Question;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.CreateExamInstanceRequest;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ClassStudentRepository;
import com.example.tracnghiem.repository.ExamInstanceRepository;
import com.example.tracnghiem.repository.ExamQuestionRepository;
import com.example.tracnghiem.repository.ExamSupervisorRepository;
import com.example.tracnghiem.repository.QuestionRepository;
import com.example.tracnghiem.repository.StudentGroupRepository;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class ExamInstanceService {

    private final ExamInstanceRepository examInstanceRepository;
    private final ExamTemplateService examTemplateService;
    private final StudentGroupRepository studentGroupRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamSupervisorRepository examSupervisorRepository;
    private final UserRepository userRepository;
    private final ClassStudentRepository classStudentRepository;

    public ExamInstanceService(ExamInstanceRepository examInstanceRepository,
                               ExamTemplateService examTemplateService,
                               StudentGroupRepository studentGroupRepository,
                               QuestionRepository questionRepository,
                               ExamQuestionRepository examQuestionRepository,
                               ExamSupervisorRepository examSupervisorRepository,
                               UserRepository userRepository,
                               ClassStudentRepository classStudentRepository) {
        this.examInstanceRepository = examInstanceRepository;
        this.examTemplateService = examTemplateService;
        this.studentGroupRepository = studentGroupRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examSupervisorRepository = examSupervisorRepository;
        this.userRepository = userRepository;
        this.classStudentRepository = classStudentRepository;
    }

    public ExamInstanceResponse createInstance(CreateExamInstanceRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Start time must be before end time");
        }
        ExamTemplate template = examTemplateService.getTemplateEntity(request.templateId());
        StudentGroup group = studentGroupRepository.findById(request.studentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Student group not found"));

        ExamInstance instance = ExamInstance.builder()
                .template(template)
                .studentGroup(group)
                .name(request.name())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .durationMinutes(request.durationMinutes() != null ? request.durationMinutes() : template.getDurationMinutes())
                .shuffleQuestions(request.shuffleQuestions())
                .shuffleOptions(request.shuffleOptions())
                .build();

        ExamInstance saved = examInstanceRepository.save(instance);
        generateExamQuestions(saved, request.shuffleQuestions());
        assignSupervisors(saved, request.supervisors());

        return toResponse(saved);
    }

    public List<ExamInstanceResponse> getInstancesForGroup(Long groupId) {
        return examInstanceRepository.findByStudentGroup_Id(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExamInstanceResponse> getUpcomingInstancesForStudent(Long studentId) {
        Instant now = Instant.now();
        return classStudentRepository.findByStudent_Id(studentId).stream()
                .map(cs -> cs.getStudentGroup().getId())
                .distinct()
                .flatMap(groupId -> examInstanceRepository.findByStudentGroup_Id(groupId).stream())
                .filter(instance -> instance.getEndTime().isAfter(now))
                .map(this::toResponse)
                .toList();
    }

    public ExamInstance getExamInstance(Long id) {
        return examInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam instance not found"));
    }

    public ExamInstanceResponse getInstanceDto(Long id) {
        return toResponse(getExamInstance(id));
    }

    public List<ExamQuestion> getExamQuestions(Long examInstanceId) {
        return examQuestionRepository.findByExamInstance_IdOrderByQuestionOrderAsc(examInstanceId);
    }

    public boolean shouldShuffleOptions(Long examInstanceId) {
        return getExamInstance(examInstanceId).isShuffleOptions();
    }

    private void generateExamQuestions(ExamInstance instance, boolean shuffleQuestions) {
        List<Question> selected = new ArrayList<>();
        for (ExamStructure structure : instance.getTemplate().getStructures()) {
            List<Question> candidates = new ArrayList<>(questionRepository.findByChapter_IdAndActiveTrue(structure.getChapter().getId()));
            if (candidates.size() < structure.getNumQuestion()) {
                throw new BadRequestException("Insufficient questions for chapter " + structure.getChapter().getName());
            }
            Collections.shuffle(candidates);
            selected.addAll(candidates.subList(0, structure.getNumQuestion()));
        }
        if (shuffleQuestions) {
            Collections.shuffle(selected);
        } else {
            selected.sort((q1, q2) -> q1.getId().compareTo(q2.getId()));
        }

        AtomicInteger order = new AtomicInteger(1);
        selected.forEach(question -> {
            ExamQuestion examQuestion = ExamQuestion.builder()
                    .id(new ExamQuestionId(instance.getId(), question.getId()))
                    .examInstance(instance)
                    .question(question)
                    .questionOrder(order.getAndIncrement())
                    .build();
            examQuestionRepository.save(examQuestion);
        });
    }

    private void assignSupervisors(ExamInstance instance, List<CreateExamInstanceRequest.SupervisorAssignment> supervisors) {
        if (supervisors == null) {
            return;
        }
        supervisors.forEach(s -> {
            User supervisor = userRepository.findById(s.supervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found: " + s.supervisorId()));
            ExamSupervisor examSupervisor = ExamSupervisor.builder()
                    .examInstance(instance)
                    .supervisor(supervisor)
                    .roomNumber(s.roomNumber())
                    .assignedAt(Instant.now())
                    .build();
            examSupervisorRepository.save(examSupervisor);
        });
    }

    private ExamInstanceResponse toResponse(ExamInstance instance) {
        List<ExamInstanceResponse.SupervisorPayload> supervisors = examSupervisorRepository.findByExamInstance_Id(instance.getId()).stream()
                .map(s -> new ExamInstanceResponse.SupervisorPayload(
                        s.getSupervisor().getId(),
                        s.getSupervisor().getFullName(),
                        s.getRoomNumber()
                )).toList();
        return new ExamInstanceResponse(
                instance.getId(),
                instance.getTemplate().getId(),
                instance.getStudentGroup().getId(),
                instance.getName(),
                instance.getStartTime(),
                instance.getEndTime(),
                instance.getDurationMinutes(),
                instance.isShuffleQuestions(),
                instance.isShuffleOptions(),
                supervisors
        );
    }
}

