package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.question.*;
import com.example.tracnghiem.domain.subject.Chapter;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.question.BulkCreateQuestionResponse;
import com.example.tracnghiem.dto.question.CreateQuestionRequest;
import com.example.tracnghiem.dto.question.QuestionFilterRequest;
import com.example.tracnghiem.dto.question.QuestionResponse;
import com.example.tracnghiem.dto.question.QuestionOptionRequest;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ChapterRepository;
import com.example.tracnghiem.repository.ExamInstanceRepository;
import com.example.tracnghiem.repository.ExamQuestionRepository;
import com.example.tracnghiem.repository.PassageRepository;
import com.example.tracnghiem.repository.QuestionRepository;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ChapterRepository chapterRepository;
    private final PassageRepository passageRepository;
    private final UserRepository userRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamInstanceRepository examInstanceRepository;

    public QuestionService(QuestionRepository questionRepository,
            ChapterRepository chapterRepository,
            PassageRepository passageRepository,
            UserRepository userRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamInstanceRepository examInstanceRepository) {
        this.questionRepository = questionRepository;
        this.chapterRepository = chapterRepository;
        this.passageRepository = passageRepository;
        this.userRepository = userRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examInstanceRepository = examInstanceRepository;
    }

    public QuestionResponse createQuestion(CreateQuestionRequest request, Long creatorId) {
        Question question = buildQuestionEntity(new Question(), request, creatorId);
        return toResponse(questionRepository.save(question));
    }

    public BulkCreateQuestionResponse bulkCreateQuestions(List<CreateQuestionRequest> requests, Long creatorId) {
        List<Question> questionsToCreate = new ArrayList<>();
        List<BulkCreateQuestionResponse.DuplicateQuestionInfo> duplicates = new ArrayList<>();
        
        // Set để track các câu hỏi đã xử lý trong cùng một lần import (tránh trùng trong file)
        Set<String> processedInFile = new HashSet<>();
        
        for (int i = 0; i < requests.size(); i++) {
            CreateQuestionRequest request = requests.get(i);
            
            // Tạo key để kiểm tra trùng trong file: chapterId + content + passageId
            String fileKey = request.chapterId() + "|" + request.content().trim() + "|" + 
                           (request.passageId() != null ? request.passageId() : "NULL");
            
            // Kiểm tra trùng trong cùng một lần import
            if (processedInFile.contains(fileKey)) {
                duplicates.add(new BulkCreateQuestionResponse.DuplicateQuestionInfo(
                    request.content(),
                    request.chapterId(),
                    request.passageId(),
                    "TRONG_FILE"
                ));
                continue;
            }
            
            // Kiểm tra trùng với câu hỏi đã tồn tại trong database
            List<Question> existingQuestions = questionRepository.findDuplicates(
                request.chapterId(),
                request.content().trim(),
                request.passageId()
            );
            
            if (!existingQuestions.isEmpty()) {
                duplicates.add(new BulkCreateQuestionResponse.DuplicateQuestionInfo(
                    request.content(),
                    request.chapterId(),
                    request.passageId(),
                    "DA_TON_TAI"
                ));
                processedInFile.add(fileKey); // Đánh dấu đã xử lý để tránh thêm vào duplicates nếu có trùng trong file
                continue;
            }
            
            // Câu hỏi không trùng, thêm vào danh sách để tạo
            Question question = buildQuestionEntity(new Question(), request, creatorId);
            questionsToCreate.add(question);
            processedInFile.add(fileKey);
        }
        
        // Lưu các câu hỏi không trùng
        List<Question> savedQuestions = questionRepository.saveAll(questionsToCreate);
        List<QuestionResponse> created = savedQuestions.stream().map(this::toResponse).toList();
        
        return new BulkCreateQuestionResponse(
            created,
            duplicates,
            requests.size(),
            created.size(),
            duplicates.size()
        );
    }

    public QuestionResponse updateQuestion(Long questionId, CreateQuestionRequest request, Long editorId) {
        Question existing = getQuestion(questionId);
        Question updated = buildQuestionEntity(existing, request, editorId);
        questionRepository.save(updated);
        return toResponse(updated);
    }

    public List<QuestionResponse> filterQuestions(QuestionFilterRequest filter) {
        return questionRepository.filter(
                filter.subjectId(),
                filter.chapterId(),
                filter.difficulty(),
                filter.createdBy(),
                filter.hasPassage(),
                filter.questionType()).stream().map(this::toResponse).toList();
    }

    public void deleteQuestion(Long id) {
        Question question = getQuestion(id);
        
        // Check if question is being used in any exam
        List<Long> examInstanceIds = examQuestionRepository.findExamInstanceIdsByQuestionId(id);
        if (!examInstanceIds.isEmpty()) {
            // Kiểm tra xem có exam nào chưa kết thúc không
            Instant now = Instant.now();
            List<Long> activeExamIds = examInstanceRepository.findAllById(examInstanceIds).stream()
                    .filter(exam -> exam.getEndTime().isAfter(now))
                    .map(exam -> exam.getId())
                    .toList();
            
            if (!activeExamIds.isEmpty()) {
                throw new BadRequestException(
                        "Không thể xóa câu hỏi này vì nó đang được sử dụng trong kỳ thi chưa kết thúc. " +
                        "Vui lòng đợi kỳ thi kết thúc hoặc vô hiệu hóa câu hỏi thay vì xóa.");
            }
            // Nếu tất cả exam đã kết thúc, cho phép xóa (dữ liệu lịch sử vẫn được giữ trong exam_questions)
        }
        
        questionRepository.delete(question);
    }

    public Question getQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    private Question buildQuestionEntity(Question question, CreateQuestionRequest request, Long creatorId) {
        Chapter chapter = chapterRepository.findById(request.chapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
        Passage passage = request.passageId() == null ? null
                : passageRepository.findById(request.passageId())
                        .orElseThrow(() -> new ResourceNotFoundException("Passage not found"));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        question.setChapter(chapter);
        question.setPassage(passage);
        question.setContent(request.content());
        question.setQuestionType(request.questionType());
        question.setDifficulty(request.difficulty());
        question.setActive(request.active());
        question.setCreatedBy(creator);

        question.getOptions().clear();
        question.getAnswers().clear();

        if (request.questionType() == QuestionType.MCQ) {
            validateMcqOptions(request);
            List<QuestionOption> options = new ArrayList<>();
            AtomicInteger order = new AtomicInteger(1);
            request.options().forEach(optionRequest -> {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .content(optionRequest.content())
                        .correct(optionRequest.correct())
                        .optionOrder(order.getAndIncrement())
                        .build();
                options.add(option);
            });
            question.getOptions().addAll(options);
        } else {
            validateFillAnswers(request);
            List<QuestionAnswer> answers = request.answers().stream()
                    .map(ans -> QuestionAnswer.builder()
                            .question(question)
                            .correctAnswer(ans.answer())
                            .build())
                    .toList();
            question.getAnswers().addAll(answers);
        }
        return question;
    }

    private void validateMcqOptions(CreateQuestionRequest request) {
        if (request.options() == null || request.options().isEmpty()) {
            throw new BadRequestException("MCQ questions require options");
        }
        boolean hasCorrect = request.options().stream().anyMatch(QuestionOptionRequest::correct);
        if (!hasCorrect) {
            throw new BadRequestException("At least one option must be correct");
        }
    }

    private void validateFillAnswers(CreateQuestionRequest request) {
        if (request.answers() == null || request.answers().isEmpty()) {
            throw new BadRequestException("Fill questions require accepted answers");
        }
    }

    private QuestionResponse toResponse(Question question) {
        List<QuestionResponse.QuestionOptionPayload> options = question.getOptions().stream()
                .map(opt -> new QuestionResponse.QuestionOptionPayload(opt.getId(), opt.getContent(), opt.isCorrect()))
                .toList();
        List<String> answers = question.getAnswers().stream()
                .map(QuestionAnswer::getCorrectAnswer)
                .toList();
        return new QuestionResponse(
                question.getId(),
                question.getChapter().getId(),
                question.getPassage() == null ? null : question.getPassage().getId(),
                question.getContent(),
                question.getQuestionType(),
                question.getDifficulty(),
                question.isActive(),
                options,
                answers);
    }
}
