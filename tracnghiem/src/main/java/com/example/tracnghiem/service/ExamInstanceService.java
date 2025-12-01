package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.*;
import com.example.tracnghiem.domain.group.StudentGroup;
import com.example.tracnghiem.domain.question.Question;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.CreateExamInstanceRequest;
import com.example.tracnghiem.dto.exam.ExamInstanceResponse;
import com.example.tracnghiem.dto.exam.UpdateExamInstanceRequest;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ClassStudentRepository;
import com.example.tracnghiem.repository.ExamAnswerRepository;
import com.example.tracnghiem.repository.ExamAttemptRepository;
import com.example.tracnghiem.repository.ExamInstanceRepository;
import com.example.tracnghiem.repository.ExamQuestionRepository;
import com.example.tracnghiem.repository.ExamSupervisorRepository;
import com.example.tracnghiem.repository.ExamViolationRepository;
import com.example.tracnghiem.repository.QuestionRepository;
import com.example.tracnghiem.repository.StudentGroupRepository;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamViolationRepository examViolationRepository;

    public ExamInstanceService(ExamInstanceRepository examInstanceRepository,
            ExamTemplateService examTemplateService,
            StudentGroupRepository studentGroupRepository,
            QuestionRepository questionRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamSupervisorRepository examSupervisorRepository,
            UserRepository userRepository,
            ClassStudentRepository classStudentRepository,
            ExamAttemptRepository examAttemptRepository,
            ExamAnswerRepository examAnswerRepository,
            ExamViolationRepository examViolationRepository) {
        this.examInstanceRepository = examInstanceRepository;
        this.examTemplateService = examTemplateService;
        this.studentGroupRepository = studentGroupRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examSupervisorRepository = examSupervisorRepository;
        this.userRepository = userRepository;
        this.classStudentRepository = classStudentRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.examViolationRepository = examViolationRepository;
    }

    public ExamInstanceResponse createInstance(CreateExamInstanceRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        ExamTemplate template = examTemplateService.getTemplateEntity(request.templateId());
        StudentGroup group = studentGroupRepository.findById(request.studentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm sinh viên"));

        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new BadRequestException("Thời lượng là bắt buộc và phải lớn hơn 0");
        }

        ExamInstance instance = ExamInstance.builder()
                .template(template)
                .studentGroup(group)
                .name(request.name())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .durationMinutes(request.durationMinutes())
                .totalMarks(request.totalMarks())
                .shuffleQuestions(request.shuffleQuestions())
                .shuffleOptions(request.shuffleOptions())
                .build();

        ExamInstance saved = examInstanceRepository.save(instance);
        generateExamQuestions(saved, request.shuffleQuestions());
        assignSupervisors(saved, request.supervisors());

        return toResponse(saved);
    }

    public ExamInstanceResponse updateInstance(Long id, UpdateExamInstanceRequest request) {
        ExamInstance instance = getExamInstance(id);
        
        // Kiểm tra nếu kỳ thi đã bắt đầu hoặc đã có học sinh làm bài thì không cho sửa một số trường
        Instant now = Instant.now();
        boolean hasAttempts = !examAttemptRepository.findByExamInstance_Id(instance.getId()).isEmpty();
        
        if (hasAttempts && (instance.getStartTime().isBefore(now) || !request.startTime().equals(instance.getStartTime()) 
                || !request.templateId().equals(instance.getTemplate().getId()))) {
            throw new BadRequestException("Không thể sửa kỳ thi đã có học sinh làm bài. Chỉ có thể sửa thông tin cơ bản như tên, giám thị.");
        }
        
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        
        ExamTemplate template = examTemplateService.getTemplateEntity(request.templateId());
        StudentGroup group = studentGroupRepository.findById(request.studentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm sinh viên"));

        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new BadRequestException("Thời lượng là bắt buộc và phải lớn hơn 0");
        }

        // Cập nhật thông tin cơ bản
        instance.setName(request.name());
        instance.setStartTime(request.startTime());
        instance.setEndTime(request.endTime());
        instance.setDurationMinutes(request.durationMinutes());
        instance.setTotalMarks(request.totalMarks());
        instance.setShuffleQuestions(request.shuffleQuestions());
        instance.setShuffleOptions(request.shuffleOptions());
        instance.setStudentGroup(group);
        
        // Nếu chưa có học sinh làm bài và template thay đổi, regenerate questions
        if (!hasAttempts && !request.templateId().equals(instance.getTemplate().getId())) {
            instance.setTemplate(template);
            // Xóa câu hỏi cũ và tạo lại
            examQuestionRepository.deleteByExamInstance_Id(instance.getId());
            generateExamQuestions(instance, request.shuffleQuestions());
        }
        
        // Cập nhật giám thị
        assignSupervisors(instance, request.supervisors());

        ExamInstance saved = examInstanceRepository.save(instance);
        return toResponse(saved);
    }

    public List<ExamInstanceResponse> getInstancesForGroup(Long groupId) {
        return examInstanceRepository.findByStudentGroup_Id(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExamInstanceResponse> getAllInstances() {
        return examInstanceRepository.findAll().stream()
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

    public List<ExamInstanceResponse> getAllInstancesForStudent(Long studentId) {
        return classStudentRepository.findByStudent_Id(studentId).stream()
                .map(cs -> cs.getStudentGroup().getId())
                .distinct()
                .flatMap(groupId -> examInstanceRepository.findByStudentGroup_Id(groupId).stream())
                .map(this::toResponse)
                .distinct()
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
        Set<Long> selectedPassageIds = new HashSet<>(); // Track passages đã được chọn
        
        for (ExamStructure structure : instance.getTemplate().getStructures()) {
            List<Question> allCandidates = new ArrayList<>(
                    questionRepository.findByChapter_IdAndActiveTrue(structure.getChapter().getId()));

            // Phân loại câu hỏi theo độ khó
            // Tách riêng câu hỏi có passage và không có passage
            List<Question> basicQuestions = new ArrayList<>();
            List<Question> advancedQuestions = new ArrayList<>();
            List<Question> basicQuestionsWithPassage = new ArrayList<>();
            List<Question> advancedQuestionsWithPassage = new ArrayList<>();

            for (Question q : allCandidates) {
                String difficulty = q.getDifficulty();
                boolean isAdvanced = difficulty != null
                        && (difficulty.equalsIgnoreCase("ADVANCED") || difficulty.equalsIgnoreCase("NÂNG CAO"));
                
                if (q.getPassage() != null) {
                    // Câu hỏi có passage
                    if (isAdvanced) {
                        advancedQuestionsWithPassage.add(q);
                    } else {
                        basicQuestionsWithPassage.add(q);
                    }
                } else {
                    // Câu hỏi không có passage
                    if (isAdvanced) {
                        advancedQuestions.add(q);
                    } else {
                        basicQuestions.add(q);
                    }
                }
            }

            int numBasic = structure.getNumBasic() != null ? structure.getNumBasic() : 0;
            int numAdvanced = structure.getNumAdvanced() != null ? structure.getNumAdvanced() : 0;

            // Đếm số câu hỏi có sẵn (bao gồm cả passage)
            int availableBasic = basicQuestions.size() + countDistinctPassageQuestions(basicQuestionsWithPassage);
            int availableAdvanced = advancedQuestions.size() + countDistinctPassageQuestions(advancedQuestionsWithPassage);

            // Kiểm tra số lượng câu hỏi có đủ không
            if (availableBasic < numBasic) {
                throw new BadRequestException(
                        "Không đủ câu hỏi cơ bản trong chương " + structure.getChapter().getName() +
                                ". Cần: " + numBasic + ", có: " + availableBasic);
            }
            if (availableAdvanced < numAdvanced) {
                throw new BadRequestException(
                        "Không đủ câu hỏi nâng cao trong chương " + structure.getChapter().getName() +
                                ". Cần: " + numAdvanced + ", có: " + availableAdvanced);
            }

            // Chọn câu hỏi cơ bản
            List<Question> selectedBasic = selectQuestionsWithPassageGrouping(
                    basicQuestions, basicQuestionsWithPassage, numBasic, selectedPassageIds);
            selected.addAll(selectedBasic);

            // Chọn câu hỏi nâng cao
            List<Question> selectedAdvanced = selectQuestionsWithPassageGrouping(
                    advancedQuestions, advancedQuestionsWithPassage, numAdvanced, selectedPassageIds);
            selected.addAll(selectedAdvanced);
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
    
    /**
     * Đếm số câu hỏi riêng biệt từ danh sách câu hỏi có passage
     * (mỗi passage chỉ tính 1 lần, nhưng số câu hỏi = số câu hỏi trong passage đó)
     */
    private int countDistinctPassageQuestions(List<Question> questionsWithPassage) {
        Set<Long> passageIds = new HashSet<>();
        int totalQuestions = 0;
        
        for (Question q : questionsWithPassage) {
            if (q.getPassage() != null && !passageIds.contains(q.getPassage().getId())) {
                passageIds.add(q.getPassage().getId());
                // Đếm tất cả câu hỏi trong passage này
                List<Question> passageQuestions = questionRepository.findByPassage_IdAndActiveTrue(q.getPassage().getId());
                totalQuestions += passageQuestions.size();
            }
        }
        
        // Thêm câu hỏi không có passage
        int questionsWithoutPassage = (int) questionsWithPassage.stream()
                .filter(q -> q.getPassage() == null)
                .count();
        totalQuestions += questionsWithoutPassage;
        
        return totalQuestions;
    }
    
    /**
     * Chọn câu hỏi với logic: nếu chọn câu hỏi có passage, phải chọn tất cả câu hỏi cùng passage
     * Đảm bảo tỉ lệ cân bằng giữa passage và câu hỏi đơn lẻ
     */
    private List<Question> selectQuestionsWithPassageGrouping(
            List<Question> questionsWithoutPassage,
            List<Question> questionsWithPassage,
            int targetCount,
            Set<Long> selectedPassageIds) {
        
        List<Question> selected = new ArrayList<>();
        Collections.shuffle(questionsWithoutPassage);
        Collections.shuffle(questionsWithPassage);
        
        // Nhóm câu hỏi theo passage
        Set<Long> processedPassageIds = new HashSet<>();
        List<List<Question>> passageGroups = new ArrayList<>();
        
        for (Question q : questionsWithPassage) {
            if (q.getPassage() != null) {
                Long passageId = q.getPassage().getId();
                if (!processedPassageIds.contains(passageId) && !selectedPassageIds.contains(passageId)) {
                    processedPassageIds.add(passageId);
                    // Lấy tất cả câu hỏi cùng passage
                    List<Question> passageQuestions = questionRepository.findByPassage_IdAndActiveTrue(passageId);
                    if (!passageQuestions.isEmpty()) {
                        passageGroups.add(passageQuestions);
                    }
                }
            }
        }
        
        // Tính tổng số câu hỏi từ passage và câu hỏi đơn lẻ
        int totalPassageQuestions = passageGroups.stream().mapToInt(List::size).sum();
        int totalStandaloneQuestions = questionsWithoutPassage.size();
        int totalAvailable = totalPassageQuestions + totalStandaloneQuestions;
        
        // Tính tỉ lệ: nếu có cả passage và standalone, đảm bảo tỉ lệ cân bằng
        // Ví dụ: nếu có 60% passage và 40% standalone, thì chọn theo tỉ lệ đó
        int passageTargetCount = 0;
        int standaloneTargetCount = 0;
        
        if (totalAvailable > 0 && totalPassageQuestions > 0 && totalStandaloneQuestions > 0) {
            // Tính tỉ lệ dựa trên số lượng có sẵn
            double passageRatio = (double) totalPassageQuestions / totalAvailable;
            passageTargetCount = (int) Math.round(targetCount * passageRatio);
            standaloneTargetCount = targetCount - passageTargetCount;
            
            // Đảm bảo không vượt quá số lượng có sẵn
            if (passageTargetCount > totalPassageQuestions) {
                passageTargetCount = totalPassageQuestions;
                standaloneTargetCount = targetCount - passageTargetCount;
            }
            if (standaloneTargetCount > totalStandaloneQuestions) {
                standaloneTargetCount = totalStandaloneQuestions;
                passageTargetCount = targetCount - standaloneTargetCount;
            }
            
            // Đảm bảo tỉ lệ tối thiểu: ít nhất 10% passage nếu có passage
            int minPassageCount = (int) Math.ceil(targetCount * 0.1);
            if (passageTargetCount < minPassageCount && totalPassageQuestions >= minPassageCount) {
                passageTargetCount = Math.min(minPassageCount, totalPassageQuestions);
                standaloneTargetCount = targetCount - passageTargetCount;
            }
        } else if (totalPassageQuestions > 0) {
            // Chỉ có passage
            passageTargetCount = Math.min(targetCount, totalPassageQuestions);
        } else {
            // Chỉ có standalone
            standaloneTargetCount = Math.min(targetCount, totalStandaloneQuestions);
        }
        
        // Chọn passage groups theo tỉ lệ
        int passageCount = 0;
        Collections.shuffle(passageGroups); // Shuffle để random
        
        for (List<Question> passageGroup : passageGroups) {
            if (passageCount >= passageTargetCount) {
                break;
            }
            int passageQuestionCount = passageGroup.size();
            if (passageCount + passageQuestionCount <= passageTargetCount) {
                selected.addAll(passageGroup);
                selectedPassageIds.add(passageGroup.get(0).getPassage().getId());
                passageCount += passageQuestionCount;
            }
        }
        
        // Chọn câu hỏi đơn lẻ theo tỉ lệ
        int standaloneCount = 0;
        for (Question q : questionsWithoutPassage) {
            if (standaloneCount >= standaloneTargetCount) {
                break;
            }
            selected.add(q);
            standaloneCount++;
        }
        
        // Nếu vẫn chưa đủ, bổ sung từ phần còn lại
        int currentCount = passageCount + standaloneCount;
        if (currentCount < targetCount) {
            // Thử thêm passage groups nhỏ hơn
            for (List<Question> passageGroup : passageGroups) {
                if (currentCount >= targetCount) {
                    break;
                }
                Long passageId = passageGroup.get(0).getPassage().getId();
                if (!selectedPassageIds.contains(passageId)) {
                    int passageQuestionCount = passageGroup.size();
                    if (currentCount + passageQuestionCount <= targetCount) {
                        selected.addAll(passageGroup);
                        selectedPassageIds.add(passageId);
                        currentCount += passageQuestionCount;
                    }
                }
            }
            
            // Thêm câu hỏi đơn lẻ nếu còn thiếu
            for (Question q : questionsWithoutPassage) {
                if (currentCount >= targetCount) {
                    break;
                }
                if (!selected.contains(q)) {
                    selected.add(q);
                    currentCount++;
                }
            }
        }
        
        if (currentCount < targetCount) {
            throw new BadRequestException(
                    "Không thể chọn đủ " + targetCount + " câu hỏi. Chỉ chọn được " + currentCount + " câu.");
        }
        
        return selected;
    }

    @Transactional
    public ExamInstanceResponse assignSupervisorsToInstance(Long examInstanceId,
            List<CreateExamInstanceRequest.SupervisorAssignment> supervisors) {
        ExamInstance instance = examInstanceRepository.findById(examInstanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam instance not found: " + examInstanceId));

        // Remove all existing supervisor assignments for this exam instance
        List<ExamSupervisor> existingAssignments = examSupervisorRepository.findByExamInstance_Id(examInstanceId);
        if (!existingAssignments.isEmpty()) {
            examSupervisorRepository.deleteAll(existingAssignments);
            examSupervisorRepository.flush(); // Ensure deletion is committed
        }

        // Add new supervisor assignments
        if (supervisors != null && !supervisors.isEmpty()) {
            supervisors.forEach(s -> {
                User supervisor = userRepository.findById(s.supervisorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found: " + s.supervisorId()));
                ExamSupervisor examSupervisor = ExamSupervisor.builder()
                        .examInstance(instance)
                        .supervisor(supervisor)
                        .assignedAt(Instant.now())
                        .build();
                examSupervisorRepository.save(examSupervisor);
            });
            examSupervisorRepository.flush(); // Ensure saves are committed
        }

        return toResponse(instance);
    }

    private void assignSupervisors(ExamInstance instance,
            List<CreateExamInstanceRequest.SupervisorAssignment> supervisors) {
        if (supervisors == null) {
            return;
        }
        supervisors.forEach(s -> {
            User supervisor = userRepository.findById(s.supervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found: " + s.supervisorId()));
            ExamSupervisor examSupervisor = ExamSupervisor.builder()
                    .examInstance(instance)
                    .supervisor(supervisor)
                    .assignedAt(Instant.now())
                    .build();
            examSupervisorRepository.save(examSupervisor);
        });
    }

    public void deleteInstance(Long id) {
        ExamInstance instance = examInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam instance not found"));

        // Delete all related data (including student attempts)
        List<ExamAttempt> attempts = examAttemptRepository.findByExamInstance_Id(id);
        if (!attempts.isEmpty()) {
            List<Long> attemptIds = attempts.stream().map(ExamAttempt::getId).toList();
            
            // 1. Delete violations for all attempts
            examViolationRepository.deleteAll(examViolationRepository.findByAttempt_ExamInstance_Id(id));
            
            // 2. Delete answers for all attempts
            examAnswerRepository.deleteAll(examAnswerRepository.findByAttempt_IdIn(attemptIds));
            
            // 3. Delete all attempts
            examAttemptRepository.deleteAll(attempts);
        }

        // 4. Delete exam questions
        examQuestionRepository.deleteAll(examQuestionRepository.findByExamInstance_IdOrderByQuestionOrderAsc(id));
        
        // 5. Delete supervisor assignments
        examSupervisorRepository.deleteAll(examSupervisorRepository.findByExamInstance_Id(id));
        
        // 6. Delete exam instance
        examInstanceRepository.delete(instance);
    }

    private ExamInstanceResponse toResponse(ExamInstance instance) {
        List<ExamInstanceResponse.SupervisorPayload> supervisors = examSupervisorRepository
                .findByExamInstance_Id(instance.getId()).stream()
                .map(s -> new ExamInstanceResponse.SupervisorPayload(
                        s.getSupervisor().getId(),
                        s.getSupervisor().getFullName()))
                .toList();
        return new ExamInstanceResponse(
                instance.getId(),
                instance.getTemplate().getId(),
                instance.getStudentGroup().getId(),
                instance.getName(),
                instance.getTemplate().getSubject().getName(),
                instance.getStartTime(),
                instance.getEndTime(),
                instance.getDurationMinutes(),
                instance.getTotalMarks(),
                instance.isShuffleQuestions(),
                instance.isShuffleOptions(),
                supervisors);
    }
}
