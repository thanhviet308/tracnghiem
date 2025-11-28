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
                // Chỉ seed dữ liệu tối thiểu (tài khoản admin) lần đầu
                // Không tạo sẵn môn học, câu hỏi, kỳ thi... để bạn tự cấu hình theo nhu cầu
                if (userRepository.count() > 0) {
                        return;
                }

                // Tạo tài khoản ADMIN mặc định
                User admin = userRepository.existsByEmail("admin@example.com")
                                ? userRepository.findByEmail("admin@example.com").orElseThrow()
                                : userRepository.save(User.builder()
                                                .fullName("System Admin")
                                                .email("admin@example.com")
                                                .passwordHash(passwordEncoder.encode("Admin123!"))
                                                .role(UserRole.ADMIN)
                                                .active(true)
                                                .build());
        }
}
