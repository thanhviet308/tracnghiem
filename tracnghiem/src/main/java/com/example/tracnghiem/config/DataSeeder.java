package com.example.tracnghiem.config;

import com.example.tracnghiem.domain.group.ClassStudent;
import com.example.tracnghiem.domain.group.ClassStudentId;
import com.example.tracnghiem.domain.group.StudentGroup;
import com.example.tracnghiem.domain.question.QuestionType;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.domain.user.UserRole;
import com.example.tracnghiem.dto.exam.CreateExamInstanceRequest;
import com.example.tracnghiem.dto.exam.CreateExamTemplateRequest;
import com.example.tracnghiem.dto.exam.ExamStructureRequest;
import com.example.tracnghiem.dto.question.CreateQuestionRequest;
import com.example.tracnghiem.dto.question.QuestionAnswerRequest;
import com.example.tracnghiem.dto.question.QuestionOptionRequest;
import com.example.tracnghiem.dto.subject.ChapterResponse;
import com.example.tracnghiem.dto.subject.ChapterRequest;
import com.example.tracnghiem.dto.subject.SubjectRequest;
import com.example.tracnghiem.dto.subject.SubjectResponse;
import com.example.tracnghiem.repository.ClassStudentRepository;
import com.example.tracnghiem.repository.StudentGroupRepository;
import com.example.tracnghiem.repository.UserRepository;
import com.example.tracnghiem.service.ExamInstanceService;
import com.example.tracnghiem.service.ExamTemplateService;
import com.example.tracnghiem.service.QuestionService;
import com.example.tracnghiem.service.SubjectService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final ClassStudentRepository classStudentRepository;
    private final SubjectService subjectService;
    private final QuestionService questionService;
    private final ExamTemplateService examTemplateService;
    private final ExamInstanceService examInstanceService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      StudentGroupRepository studentGroupRepository,
                      ClassStudentRepository classStudentRepository,
                      SubjectService subjectService,
                      QuestionService questionService,
                      ExamTemplateService examTemplateService,
                      ExamInstanceService examInstanceService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentGroupRepository = studentGroupRepository;
        this.classStudentRepository = classStudentRepository;
        this.subjectService = subjectService;
        this.questionService = questionService;
        this.examTemplateService = examTemplateService;
        this.examInstanceService = examInstanceService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = userRepository.save(User.builder()
                .fullName("System Admin")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        User teacher = userRepository.save(User.builder()
                .fullName("Alice Teacher")
                .email("teacher@example.com")
                .passwordHash(passwordEncoder.encode("Teacher123!"))
                .role(UserRole.TEACHER)
                .active(true)
                .build());

        User student = userRepository.save(User.builder()
                .fullName("Bob Student")
                .email("student@example.com")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .role(UserRole.STUDENT)
                .active(true)
                .build());

        User supervisor = userRepository.save(User.builder()
                .fullName("Sara Supervisor")
                .email("supervisor@example.com")
                .passwordHash(passwordEncoder.encode("Supervisor123!"))
                .role(UserRole.SUPERVISOR)
                .active(true)
                .build());

        StudentGroup group = studentGroupRepository.save(StudentGroup.builder()
                .name("K67-CTT1")
                .build());

        classStudentRepository.save(ClassStudent.builder()
                .id(new ClassStudentId(group.getId(), student.getId()))
                .studentGroup(group)
                .student(student)
                .build());

        SubjectResponse subject = subjectService.createSubject(new SubjectRequest("Computer Science", "Intro CS", true));
        ChapterResponse algorithmsChapter = subjectService.createChapter(new ChapterRequest(subject.id(), "Algorithms", "Sorting and searching"));
        ChapterResponse dbChapter = subjectService.createChapter(new ChapterRequest(subject.id(), "Databases", "SQL basics"));

        questionService.createQuestion(
                new CreateQuestionRequest(
                        algorithmsChapter.id(),
                        null,
                        "Which of the following sorting algorithms has O(n log n) average complexity?",
                        QuestionType.MCQ,
                        "MEDIUM",
                        2,
                        true,
                        List.of(
                                new QuestionOptionRequest("Bubble sort", false),
                                new QuestionOptionRequest("Merge sort", true),
                                new QuestionOptionRequest("Insertion sort", false),
                                new QuestionOptionRequest("Selection sort", false)
                        ),
                        null
                ),
                teacher.getId()
        );

        questionService.createQuestion(
                new CreateQuestionRequest(
                        dbChapter.id(),
                        null,
                        "Fill in the blank: ____ key uniquely identifies a record in a table.",
                        QuestionType.FILL,
                        "EASY",
                        1,
                        true,
                        null,
                        List.of(new QuestionAnswerRequest("Primary"), new QuestionAnswerRequest("Primary key"))
                ),
                teacher.getId()
        );

        CreateExamTemplateRequest templateRequest = new CreateExamTemplateRequest(
                subject.id(),
                "Midterm Template",
                2,
                60,
                List.of(
                        new ExamStructureRequest(algorithmsChapter.id(), 1),
                        new ExamStructureRequest(dbChapter.id(), 1)
                )
        );

        var template = examTemplateService.createTemplate(templateRequest, teacher.getId());

        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        CreateExamInstanceRequest instanceRequest = new CreateExamInstanceRequest(
                template.id(),
                group.getId(),
                "Midterm Attempt 1",
                start,
                end,
                template.durationMinutes(),
                true,
                true,
                List.of(new CreateExamInstanceRequest.SupervisorAssignment(supervisor.getId(), "LAB-1"))
        );
        examInstanceService.createInstance(instanceRequest);
    }
}

