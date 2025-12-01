package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.exam.ExamAnswer;
import com.example.tracnghiem.domain.exam.ExamAttempt;
import com.example.tracnghiem.domain.exam.ExamAttemptStatus;
import com.example.tracnghiem.domain.exam.ExamInstance;
import com.example.tracnghiem.domain.exam.ExamQuestion;
import com.example.tracnghiem.domain.exam.ExamQuestionId;
import com.example.tracnghiem.domain.group.ClassStudentId;
import com.example.tracnghiem.domain.question.Question;
import com.example.tracnghiem.domain.question.QuestionOption;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.dto.exam.*;
import com.example.tracnghiem.exception.BadRequestException;
import com.example.tracnghiem.exception.ForbiddenException;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ClassStudentRepository;
import com.example.tracnghiem.repository.ExamAnswerRepository;
import com.example.tracnghiem.repository.ExamAttemptRepository;
import com.example.tracnghiem.repository.ExamQuestionRepository;
import com.example.tracnghiem.repository.QuestionAnswerRepository;
import com.example.tracnghiem.repository.QuestionOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.tracnghiem.util.TextUtils.normalizeAnswer;

@Service
@Transactional
public class ExamAttemptService {

    private final ExamAttemptRepository examAttemptRepository;
    private final ExamInstanceService examInstanceService;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ExamQuestionRepository examQuestionRepository;

    public ExamAttemptService(ExamAttemptRepository examAttemptRepository,
            ExamInstanceService examInstanceService,
            ExamAnswerRepository examAnswerRepository,
            QuestionOptionRepository questionOptionRepository,
            QuestionAnswerRepository questionAnswerRepository,
            ClassStudentRepository classStudentRepository,
            ExamQuestionRepository examQuestionRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examInstanceService = examInstanceService;
        this.examAnswerRepository = examAnswerRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.classStudentRepository = classStudentRepository;
        this.examQuestionRepository = examQuestionRepository;
    }

    public StartAttemptResponse startAttempt(Long examInstanceId, User student) {
        ExamInstance examInstance = examInstanceService.getExamInstance(examInstanceId);
        ensureStudentBelongsToGroup(examInstance, student);
        Instant now = Instant.now();
        if (now.isBefore(examInstance.getStartTime()) || now.isAfter(examInstance.getEndTime())) {
            throw new BadRequestException("Kỳ thi chưa bắt đầu hoặc đã kết thúc");
        }

        ExamAttempt attempt = examAttemptRepository.findByExamInstance_IdAndStudent_Id(examInstanceId, student.getId())
                .orElseGet(() -> examAttemptRepository.save(ExamAttempt.builder()
                        .examInstance(examInstance)
                        .student(student)
                        .status(ExamAttemptStatus.IN_PROGRESS)
                        .startedAt(now)
                        .build()));

        // Prevent starting a new attempt if already submitted or graded
        if (attempt.getStatus() == ExamAttemptStatus.SUBMITTED || attempt.getStatus() == ExamAttemptStatus.GRADED) {
            throw new BadRequestException("Bạn đã hoàn thành bài thi này. Không thể thi lại.");
        }

        if (attempt.getStatus() == ExamAttemptStatus.NOT_STARTED) {
            attempt.setStatus(ExamAttemptStatus.IN_PROGRESS);
        }
        if (attempt.getStartedAt() == null) {
            attempt.setStartedAt(now);
        }
        examAttemptRepository.save(attempt);

        Instant expiresAt = calculateExpiry(attempt);
        List<StartAttemptResponse.ExamQuestionView> questions = buildQuestionView(examInstance, attempt);
        return new StartAttemptResponse(attempt.getId(), examInstanceId, attempt.getStatus(),
                attempt.getStartedAt(), expiresAt, questions);
    }

    public ExamAttemptResponse answerQuestion(Long attemptId, User student, AnswerQuestionRequest request) {
        ExamAttempt attempt = getAttemptForStudent(attemptId, student);
        if (attempt.getStatus() != ExamAttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Bài thi không còn hoạt động");
        }
        Instant now = Instant.now();
        if (now.isAfter(calculateExpiry(attempt))) {
            throw new BadRequestException("Đã hết thời gian làm bài");
        }

        ExamQuestion examQuestion = examQuestionRepository.findById(new ExamQuestionId(
                attempt.getExamInstance().getId(),
                request.questionId())).orElseThrow(() -> new BadRequestException("Câu hỏi không thuộc đề thi này"));

        Question question = examQuestion.getQuestion();
        ExamAnswer answer = examAnswerRepository.findByAttempt_IdAndQuestion_Id(attempt.getId(), question.getId())
                .orElse(ExamAnswer.builder()
                        .attempt(attempt)
                        .question(question)
                        .build());
        answer.setAnsweredAt(now);

        if (question.getQuestionType() == com.example.tracnghiem.domain.question.QuestionType.MCQ) {
            if (request.selectedOptionId() == null) {
                throw new BadRequestException("Vui lòng chọn đáp án cho câu hỏi trắc nghiệm");
            }
            QuestionOption selectedOption = questionOptionRepository.findById(request.selectedOptionId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy đáp án được chọn"));
            if (!selectedOption.getQuestion().getId().equals(question.getId())) {
                throw new BadRequestException("Đáp án không thuộc câu hỏi này");
            }
            answer.setSelectedOption(selectedOption);
            answer.setFillAnswer(null);
            answer.setCorrect(selectedOption.isCorrect());
        } else {
            if (request.fillAnswer() == null || request.fillAnswer().isBlank()) {
                throw new BadRequestException("Vui lòng nhập đáp án cho câu hỏi tự luận");
            }
            answer.setFillAnswer(request.fillAnswer());
            answer.setSelectedOption(null);
            String normalized = normalizeAnswer(request.fillAnswer());
            boolean correct = questionAnswerRepository.findByQuestion_Id(question.getId()).stream()
                    .map(a -> normalizeAnswer(a.getCorrectAnswer()))
                    .anyMatch(ans -> ans.equals(normalized));
            answer.setCorrect(correct);
        }
        examAnswerRepository.save(answer);
        return toAttemptResponse(attempt);
    }

    public SubmitAttemptResponse submitAttempt(Long attemptId, User student) {
        ExamAttempt attempt = getAttemptForStudent(attemptId, student);
        if (attempt.getStatus() != ExamAttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Bài thi đã được nộp");
        }
        Instant now = Instant.now();
        if (now.isAfter(calculateExpiry(attempt))) {
            throw new BadRequestException("Đã hết thời gian làm bài");
        }
        // Tính điểm động: (tổng điểm / số câu hỏi) * số câu đúng
        List<ExamQuestion> examQuestions = examInstanceService.getExamQuestions(attempt.getExamInstance().getId());
        int totalQuestions = examQuestions.size();
        int correctAnswers = (int) examAnswerRepository.findByAttempt_Id(attempt.getId()).stream()
                .filter(ans -> Boolean.TRUE.equals(ans.getCorrect()))
                .count();
        int totalMarks = attempt.getExamInstance().getTotalMarks();
        int score = totalQuestions > 0 ? (int) Math.round((double) totalMarks * correctAnswers / totalQuestions) : 0;
        attempt.setScore(score);
        attempt.setSubmittedAt(now);
        attempt.setStatus(ExamAttemptStatus.SUBMITTED);
        examAttemptRepository.save(attempt);
        return new SubmitAttemptResponse(attempt.getId(), score, now);
    }

    public List<ExamAttemptResponse> getAttemptsForInstance(Long examInstanceId) {
        return examAttemptRepository.findByExamInstance_Id(examInstanceId).stream()
                .map(this::toAttemptResponse)
                .toList();
    }

    public List<ExamAttemptResponse> getStudentHistory(Long studentId) {
        return examAttemptRepository.findByStudent_Id(studentId).stream()
                .map(this::toAttemptResponse)
                .toList();
    }

    public ExamAttemptDetailResponse getAttemptDetail(Long attemptId, User requester) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        if (!Objects.equals(attempt.getStudent().getId(), requester.getId())
                && requester.getRole() == com.example.tracnghiem.domain.user.UserRole.STUDENT) {
            throw new ForbiddenException("Cannot view attempt");
        }

        // Get all questions in the exam
        List<ExamQuestion> examQuestions = examInstanceService.getExamQuestions(attempt.getExamInstance().getId());

        // Tính điểm cho mỗi câu hỏi: tổng điểm / số câu hỏi
        int totalMarks = attempt.getExamInstance().getTotalMarks();
        int totalQuestions = examQuestions.size();
        int marksPerQuestion = totalQuestions > 0 ? totalMarks / totalQuestions : 0;

        // Get answers for this attempt
        List<ExamAnswer> answers = examAnswerRepository.findByAttempt_Id(attemptId);
        Map<Long, ExamAnswer> answerMap = answers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getQuestion().getId(),
                        a -> a,
                        (a1, a2) -> a1));

        // Build answer views for all questions in the exam
        List<ExamAttemptDetailResponse.QuestionAnswerView> answerViews = examQuestions.stream()
                .map(eq -> {
                    ExamAnswer answer = answerMap.get(eq.getQuestion().getId());
                    if (answer != null) {
                        return toQuestionAnswerView(answer, marksPerQuestion);
                    } else {
                        // Question not answered - create a view with no answer
                        Question question = eq.getQuestion();
                        List<String> correctAnswers;
                        if (question.getQuestionType() == com.example.tracnghiem.domain.question.QuestionType.MCQ) {
                            correctAnswers = question.getOptions().stream()
                                    .filter(QuestionOption::isCorrect)
                                    .map(QuestionOption::getContent)
                                    .toList();
                        } else {
                            correctAnswers = questionAnswerRepository.findByQuestion_Id(question.getId()).stream()
                                    .map(a -> a.getCorrectAnswer())
                                    .toList();
                        }
                        return new ExamAttemptDetailResponse.QuestionAnswerView(
                                question.getId(),
                                question.getContent(),
                                question.getQuestionType().name(),
                                marksPerQuestion,
                                null, // No selected option ID
                                null, // No selected option content
                                null, // No fill answer
                                false, // Not correct (not answered)
                                correctAnswers);
                    }
                })
                .toList();

        return new ExamAttemptDetailResponse(toAttemptResponse(attempt), answerViews);
    }

    private ExamAttemptDetailResponse.QuestionAnswerView toQuestionAnswerView(ExamAnswer answer, int marksPerQuestion) {
        Question question = answer.getQuestion();
        List<String> correctAnswers;
        if (question.getQuestionType() == com.example.tracnghiem.domain.question.QuestionType.MCQ) {
            correctAnswers = question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .map(QuestionOption::getContent)
                    .toList();
        } else {
            correctAnswers = questionAnswerRepository.findByQuestion_Id(question.getId()).stream()
                    .map(a -> a.getCorrectAnswer())
                    .toList();
        }
        return new ExamAttemptDetailResponse.QuestionAnswerView(
                question.getId(),
                question.getContent(),
                question.getQuestionType().name(),
                marksPerQuestion,
                answer.getSelectedOption() == null ? null : answer.getSelectedOption().getId(),
                answer.getSelectedOption() == null ? null : answer.getSelectedOption().getContent(),
                answer.getFillAnswer(),
                Boolean.TRUE.equals(answer.getCorrect()),
                correctAnswers);
    }

    private void ensureStudentBelongsToGroup(ExamInstance examInstance, User student) {
        ClassStudentId id = new ClassStudentId(examInstance.getStudentGroup().getId(), student.getId());
        if (!classStudentRepository.existsById(id)) {
            throw new ForbiddenException("Student not in this group");
        }
    }

    private ExamAttempt getAttemptForStudent(Long attemptId, User student) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài làm"));
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("Bài làm không thuộc về sinh viên này");
        }
        return attempt;
    }

    private Instant calculateExpiry(ExamAttempt attempt) {
        Instant byDuration = attempt.getStartedAt().plus(attempt.getExamInstance().getDurationMinutes(),
                ChronoUnit.MINUTES);
        return byDuration.isBefore(attempt.getExamInstance().getEndTime()) ? byDuration
                : attempt.getExamInstance().getEndTime();
    }

    private List<StartAttemptResponse.ExamQuestionView> buildQuestionView(ExamInstance examInstance,
            ExamAttempt attempt) {
        boolean shuffleQuestions = examInstance.isShuffleQuestions();
        boolean shuffleOptions = examInstance.isShuffleOptions();
        List<ExamQuestion> examQuestions = new ArrayList<>(examInstanceService.getExamQuestions(examInstance.getId()));
        
        // Shuffle thứ tự câu hỏi nếu được bật, sử dụng attemptId + studentId làm seed để mỗi sinh viên có thứ tự khác nhau
        if (shuffleQuestions) {
            long seed = attempt.getId() * 31L + attempt.getStudent().getId() * 17L;
            Collections.shuffle(examQuestions, new Random(seed));
        }
        
        // Tính điểm cho mỗi câu hỏi: tổng điểm / số câu hỏi
        int totalMarks = examInstance.getTotalMarks();
        int totalQuestions = examQuestions.size();
        int marksPerQuestion = totalQuestions > 0 ? totalMarks / totalQuestions : 0;

        return examQuestions.stream()
                .map(eq -> toQuestionView(eq, shuffleOptions, attempt, marksPerQuestion))
                .toList();
    }

    private StartAttemptResponse.ExamQuestionView toQuestionView(ExamQuestion examQuestion, boolean shuffleOptions,
            ExamAttempt attempt, int marksPerQuestion) {
        Question question = examQuestion.getQuestion();
        List<QuestionOption> options = new ArrayList<>(question.getOptions());
        options.sort(Comparator.comparing(opt -> Optional.ofNullable(opt.getOptionOrder()).orElse(Integer.MAX_VALUE)));
        if (shuffleOptions) {
            // Sử dụng attemptId + studentId + questionId làm seed để mỗi sinh viên có thứ tự đáp án khác nhau
            long seed = attempt.getId() * 31L + attempt.getStudent().getId() * 17L + question.getId() * 7L;
            Collections.shuffle(options, new Random(seed));
        }
        List<StartAttemptResponse.OptionView> optionViews = options.stream()
                .map(opt -> new StartAttemptResponse.OptionView(opt.getId(), opt.getContent()))
                .toList();

        // Get passage info if question has a passage
        Long passageId = question.getPassage() != null ? question.getPassage().getId() : null;
        String passageContent = question.getPassage() != null ? question.getPassage().getContent() : null;

        return new StartAttemptResponse.ExamQuestionView(
                question.getId(),
                question.getContent(),
                question.getQuestionType().name(),
                marksPerQuestion,
                passageId,
                passageContent,
                optionViews);
    }

    private ExamAttemptResponse toAttemptResponse(ExamAttempt attempt) {
        return new ExamAttemptResponse(
                attempt.getId(),
                attempt.getExamInstance().getId(),
                attempt.getStudent().getId(),
                attempt.getStudent().getFullName(),
                attempt.getStudent().getEmail(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getScore(),
                attempt.getStatus());
    }
}
