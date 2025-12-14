# TỔNG HỢP CÁC CHỨC NĂNG HỆ THỐNG VÀ PHƯƠNG THỨC XỬ LÝ

## 📋 MỤC LỤC
1. [Xác thực và Phân quyền](#1-xác-thực-và-phân-quyền)
2. [Quản lý Người dùng](#2-quản-lý-người-dùng)
3. [Quản lý Môn học](#3-quản-lý-môn-học)
4. [Quản lý Nhóm sinh viên](#4-quản-lý-nhóm-sinh-viên)
5. [Quản lý Câu hỏi](#5-quản-lý-câu-hỏi)
6. [Quản lý Khung đề thi](#6-quản-lý-khung-đề-thi)
7. [Quản lý Kỳ thi](#7-quản-lý-kỳ-thi)
8. [Làm bài thi](#8-làm-bài-thi)
9. [Giám sát thi](#9-giám-sát-thi)
10. [Thống kê](#10-thống-kê)

---

## 1. XÁC THỰC VÀ PHÂN QUYỀN

### Controller: `AuthController`
**Path:** `/api/auth`

### Chức năng 1.1: Đăng ký tài khoản
- **API:** `POST /api/auth/register`
- **Phương thức:** `AuthService.register(RegisterRequest request)`
- **Mô tả:** Tạo tài khoản mới, hash mật khẩu, tạo JWT token
- **Trả về:** `TokenResponse` (accessToken, refreshToken)

### Chức năng 1.2: Đăng nhập
- **API:** `POST /api/auth/login`
- **Phương thức:** `AuthService.login(LoginRequest request)`
- **Mô tả:** Xác thực email/password, tạo JWT token
- **Trả về:** `TokenResponse`

### Chức năng 1.3: Làm mới token
- **API:** `POST /api/auth/refresh`
- **Phương thức:** `AuthService.refresh(String refreshToken)`
- **Mô tả:** Tạo accessToken mới từ refreshToken
- **Trả về:** `TokenResponse`

---

## 2. QUẢN LÝ NGƯỜI DÙNG

### Controller: `UserController`
**Path:** `/api/users`

### Chức năng 2.1: Xem danh sách người dùng
- **API:** `GET /api/users?role=STUDENT`
- **Phương thức:** `UserService.listUsers()` hoặc `UserService.listUsersByRole(UserRole role)`
- **Phân quyền:** ADMIN (xem tất cả), TEACHER/SUPERVISOR (xem theo role)
- **Trả về:** `List<UserResponse>`

### Chức năng 2.2: Xem thông tin người dùng
- **API:** `GET /api/users/{id}` hoặc `GET /api/users/me`
- **Phương thức:** `UserService.getUser(Long id)`
- **Trả về:** `UserResponse`

### Chức năng 2.3: Tạo người dùng
- **API:** `POST /api/users`
- **Phương thức:** `UserService.createUser(CreateUserRequest request)`
- **Phân quyền:** ADMIN
- **Mô tả:** Tạo tài khoản mới (ADMIN, TEACHER, STUDENT, SUPERVISOR)

### Chức năng 2.4: Cập nhật người dùng
- **API:** `PUT /api/users/{id}`
- **Phương thức:** `UserService.updateUser(Long id, UpdateUserRequest request)`
- **Phân quyền:** ADMIN

### Chức năng 2.5: Xóa người dùng
- **API:** `DELETE /api/users/{id}`
- **Phương thức:** `UserService.deleteUser(Long id)`
- **Phân quyền:** ADMIN

---

## 3. QUẢN LÝ MÔN HỌC

### Controller: `SubjectController`
**Path:** `/api/subjects`

### Chức năng 3.1: Xem danh sách môn học
- **API:** `GET /api/subjects`
- **Phương thức:** `SubjectService.getSubjects()`
- **Trả về:** `List<SubjectResponse>`

### Chức năng 3.2: Tạo môn học
- **API:** `POST /api/subjects`
- **Phương thức:** `SubjectService.createSubject(SubjectRequest request)`
- **Phân quyền:** ADMIN, TEACHER

### Chức năng 3.3: Cập nhật môn học
- **API:** `PUT /api/subjects/{id}`
- **Phương thức:** `SubjectService.updateSubject(Long id, SubjectRequest request)`
- **Phân quyền:** ADMIN, TEACHER

### Chức năng 3.4: Xóa môn học
- **API:** `DELETE /api/subjects/{id}`
- **Phương thức:** `SubjectService.deleteSubject(Long id)`
- **Phân quyền:** ADMIN, TEACHER

### Chức năng 3.5: Quản lý Chương (Chapter)
- **API:** 
  - `GET /api/subjects/{subjectId}/chapters` - Xem danh sách chương
  - `POST /api/subjects/chapters` - Tạo chương
  - `PUT /api/subjects/chapters/{id}` - Cập nhật chương
- **Phương thức:** 
  - `SubjectService.getChapters(Long subjectId)`
  - `SubjectService.createChapter(ChapterRequest request)`
  - `SubjectService.updateChapter(Long id, ChapterRequest request)`

### Chức năng 3.6: Quản lý Đoạn văn (Passage)
- **API:**
  - `GET /api/subjects/chapters/{chapterId}/passages` - Xem danh sách đoạn văn
  - `POST /api/subjects/passages` - Tạo đoạn văn
  - `PUT /api/subjects/passages/{id}` - Cập nhật đoạn văn
- **Phương thức:**
  - `SubjectService.getPassages(Long chapterId)`
  - `SubjectService.createPassage(PassageRequest request)`
  - `SubjectService.updatePassage(Long id, PassageRequest request)`

---

## 4. QUẢN LÝ NHÓM SINH VIÊN

### Controller: `StudentGroupController`
**Path:** `/api/student-groups`

### Chức năng 4.1: Xem danh sách nhóm
- **API:** `GET /api/student-groups`
- **Phương thức:** `StudentGroupService.listGroups()`
- **Phân quyền:** ADMIN, TEACHER, SUPERVISOR
- **Trả về:** `List<StudentGroupResponse>`

### Chức năng 4.2: Tạo nhóm
- **API:** `POST /api/student-groups`
- **Phương thức:** `StudentGroupService.createGroup(StudentGroupRequest request)`
- **Phân quyền:** ADMIN

### Chức năng 4.3: Cập nhật nhóm
- **API:** `PUT /api/student-groups/{id}`
- **Phương thức:** `StudentGroupService.updateGroup(Long id, StudentGroupRequest request)`
- **Phân quyền:** ADMIN

### Chức năng 4.4: Xóa nhóm
- **API:** `DELETE /api/student-groups/{id}`
- **Phương thức:** `StudentGroupService.deleteGroup(Long id)`
- **Phân quyền:** ADMIN

### Chức năng 4.5: Gán sinh viên vào nhóm
- **API:** `PUT /api/student-groups/{id}/students`
- **Phương thức:** `StudentGroupService.assignStudents(Long groupId, List<Long> studentIds)`
- **Phân quyền:** ADMIN
- **Mô tả:** Xóa tất cả mapping cũ, tạo mapping mới cho danh sách sinh viên

### Chức năng 4.6: Xem danh sách sinh viên trong nhóm
- **API:** `GET /api/student-groups/{id}/students`
- **Phương thức:** `StudentGroupService.getStudentsInGroup(Long groupId)`
- **Phân quyền:** ADMIN, TEACHER, SUPERVISOR
- **Trả về:** `List<UserResponse>`

### Chức năng 4.7: Phân công môn học cho nhóm
- **API:**
  - `GET /api/student-groups/subjects` - Xem tất cả phân công
  - `GET /api/student-groups/subjects/my` - Xem phân công của giáo viên
  - `POST /api/student-groups/subjects` - Tạo phân công
  - `PUT /api/student-groups/subjects?groupId=1&subjectId=1` - Cập nhật phân công
  - `DELETE /api/student-groups/subjects?groupId=1&subjectId=1` - Xóa phân công
- **Phương thức:**
  - `StudentGroupService.listAssignments()`
  - `StudentGroupService.getAssignmentsByTeacher(Long teacherId)`
  - `StudentGroupService.createAssignment(StudentGroupSubjectRequest request)`
  - `StudentGroupService.updateAssignment(Long groupId, Long subjectId, StudentGroupSubjectRequest request)`
  - `StudentGroupService.deleteAssignment(Long groupId, Long subjectId)`
- **Mô tả:** Gán giáo viên dạy môn học cho nhóm sinh viên

---

## 5. QUẢN LÝ CÂU HỎI

### Controller: `QuestionController`
**Path:** `/api/questions`

### Chức năng 5.1: Tìm kiếm/Lọc câu hỏi
- **API:** `GET /api/questions?subjectId=1&chapterId=2&difficulty=BASIC&questionType=MCQ`
- **Phương thức:** `QuestionService.filterQuestions(QuestionFilterRequest filter)`
- **Phân quyền:** ADMIN, TEACHER
- **Trả về:** `List<QuestionResponse>`
- **Mô tả:** Lọc câu hỏi theo môn, chương, độ khó, loại câu hỏi, người tạo

### Chức năng 5.2: Tạo câu hỏi
- **API:** `POST /api/questions`
- **Phương thức:** `QuestionService.createQuestion(CreateQuestionRequest request, Long creatorId)`
- **Phân quyền:** ADMIN, TEACHER
- **Mô tả:** 
  - Tạo câu hỏi MCQ: phải có ít nhất 1 option đúng (validate trong `validateMcqOptions()`)
  - Tạo câu hỏi FILL: phải có ít nhất 1 đáp án (validate trong `validateFillAnswers()`)
  - Logic xử lý trong `buildQuestionEntity()`

### Chức năng 5.3: Cập nhật câu hỏi
- **API:** `PUT /api/questions/{id}`
- **Phương thức:** `QuestionService.updateQuestion(Long questionId, CreateQuestionRequest request, Long editorId)`
- **Phân quyền:** ADMIN, TEACHER

### Chức năng 5.4: Xóa câu hỏi
- **API:** `DELETE /api/questions/{id}`
- **Phương thức:** `QuestionService.deleteQuestion(Long id)`
- **Phân quyền:** ADMIN, TEACHER
- **Mô tả:** 
  - Kiểm tra xem câu hỏi có đang được dùng trong kỳ thi chưa kết thúc không
  - Nếu có → throw exception
  - Nếu không → cho phép xóa

### Chức năng 5.5: Import câu hỏi hàng loạt
- **API:** `POST /api/questions/bulk`
- **Phương thức:** `QuestionService.bulkCreateQuestions(List<CreateQuestionRequest> requests, Long creatorId)`
- **Phân quyền:** ADMIN, TEACHER
- **Mô tả:**
  - Kiểm tra trùng trong file (cùng một lần import)
  - Kiểm tra trùng với câu hỏi đã tồn tại trong database
  - Chỉ tạo các câu hỏi không trùng
  - Trả về: `BulkCreateQuestionResponse` (created, duplicates, totalProcessed, totalCreated, totalDuplicates)

---

## 6. QUẢN LÝ KHUNG ĐỀ THI

### Controller: `ExamTemplateController`
**Path:** `/api/exam-templates`

### Chức năng 6.1: Tạo khung đề thi
- **API:** `POST /api/exam-templates`
- **Phương thức:** `ExamTemplateService.createTemplate(CreateExamTemplateRequest request, Long creatorId)`
- **Phân quyền:** ADMIN, TEACHER, SUPERVISOR
- **Mô tả:**
  - Tạo khung đề với `totalQuestions` (ví dụ: 30 câu)
  - Có thể tạo trước, sau đó mới cấu hình cấu trúc (structures)
  - Validate: Nếu có structures thì tổng số câu trong structures phải = totalQuestions
  - Logic validate trong `validateStructure()`
  - Kiểm tra số câu hỏi có đủ trong mỗi chương không

### Chức năng 6.2: Xem danh sách khung đề
- **API:** `GET /api/exam-templates` hoặc `GET /api/exam-templates?subjectId=1`
- **Phương thức:** 
  - `ExamTemplateService.getAllTemplates()` - Tất cả
  - `ExamTemplateService.getTemplatesBySubject(Long subjectId)` - Theo môn
- **Trả về:** `List<ExamTemplateResponse>`

### Chức năng 6.3: Xem chi tiết khung đề
- **API:** `GET /api/exam-templates/{id}`
- **Phương thức:** `ExamTemplateService.getTemplate(Long id)`
- **Trả về:** `ExamTemplateResponse` (bao gồm structures)

### Chức năng 6.4: Cập nhật khung đề
- **API:** `PUT /api/exam-templates/{id}`
- **Phương thức:** `ExamTemplateService.updateTemplate(Long id, UpdateExamTemplateRequest request)`
- **Phân quyền:** ADMIN, TEACHER, SUPERVISOR
- **Mô tả:**
  - Xóa tất cả structures cũ
  - Tạo lại structures mới
  - Validate: Tổng số câu trong structures = totalQuestions
  - Validate: numBasic + numAdvanced = numQuestion cho mỗi structure

---

## 7. QUẢN LÝ KỲ THI

### Controller: `ExamInstanceController`
**Path:** `/api/exam-instances`

### Chức năng 7.1: Tạo kỳ thi
- **API:** `POST /api/exam-instances`
- **Phương thức:** `ExamInstanceService.createInstance(CreateExamInstanceRequest request)`
- **Phân quyền:** ADMIN, TEACHER
- **Mô tả:**
  - Tạo kỳ thi từ khung đề (template)
  - Gán cho nhóm sinh viên (studentGroup)
  - Thiết lập thời gian bắt đầu, kết thúc, thời lượng
  - Tự động generate câu hỏi từ template (trong `generateExamQuestions()`)
  - Có thể bật/tắt shuffle questions và shuffle options

### Chức năng 7.2: Xem danh sách kỳ thi
- **API:** 
  - `GET /api/exam-instances` - Tất cả (ADMIN, TEACHER)
  - `GET /api/exam-instances/group/{groupId}` - Theo nhóm
  - `GET /api/exam-instances/my` - Kỳ thi của sinh viên (sắp tới)
  - `GET /api/exam-instances/my/all` - Tất cả kỳ thi của sinh viên
- **Phương thức:**
  - `ExamInstanceService.getAllInstances()`
  - `ExamInstanceService.getInstancesForGroup(Long groupId)`
  - `ExamInstanceService.getUpcomingInstancesForStudent(Long studentId)`
  - `ExamInstanceService.getAllInstancesForStudent(Long studentId)`

### Chức năng 7.3: Cập nhật kỳ thi
- **API:** `PUT /api/exam-instances/{id}`
- **Phương thức:** `ExamInstanceService.updateInstance(Long id, UpdateExamInstanceRequest request)`
- **Phân quyền:** ADMIN, TEACHER
- **Mô tả:**
  - Nếu kỳ thi đã có học sinh làm bài → không cho sửa template, startTime, duration
  - Chỉ cho sửa: tên, totalMarks, supervisors
  - Nếu chưa có học sinh làm bài và template thay đổi → regenerate questions

### Chức năng 7.4: Xóa kỳ thi
- **API:** `DELETE /api/exam-instances/{id}`
- **Phương thức:** `ExamInstanceService.deleteInstance(Long id)`
- **Phân quyền:** ADMIN, TEACHER

### Chức năng 7.5: Phân công giám thị
- **API:** `POST /api/exam-instances/{id}/supervisors`
- **Phương thức:** `ExamInstanceService.assignSupervisorsToInstance(Long id, List<SupervisorAssignment> supervisors)`
- **Phân quyền:** ADMIN, TEACHER
- **Mô tả:** Gán giám thị để giám sát kỳ thi

---

## 8. LÀM BÀI THI

### Controller: `ExamAttemptController`
**Path:** `/api/exam-attempts`

### Chức năng 8.1: Bắt đầu làm bài
- **API:** `POST /api/exam-attempts/{examInstanceId}/start`
- **Phương thức:** `ExamAttemptService.startAttempt(Long examInstanceId, User student)`
- **Phân quyền:** STUDENT
- **Mô tả:**
  - Kiểm tra sinh viên có thuộc nhóm của kỳ thi không
  - Kiểm tra thời gian (phải trong khoảng startTime - endTime)
  - Tạo ExamAttempt với status = IN_PROGRESS
  - Generate câu hỏi từ template (trong `buildQuestionView()`)
  - **Shuffle questions:** Nếu bật → shuffle thứ tự câu hỏi (seed = attemptId * 31 + studentId * 17)
  - **Shuffle options:** Nếu bật → shuffle thứ tự đáp án (seed = attemptId * 31 + studentId * 17 + questionId * 7)
  - Tính thời gian hết hạn (startedAt + durationMinutes)
  - Trả về: `StartAttemptResponse` (questions, expiresAt)

### Chức năng 8.2: Trả lời câu hỏi
- **API:** `POST /api/exam-attempts/{attemptId}/answers`
- **Phương thức:** `ExamAttemptService.answerQuestion(Long attemptId, User student, AnswerQuestionRequest request)`
- **Phân quyền:** STUDENT
- **Mô tả:**
  - Lưu câu trả lời vào bảng `dap_an_bai_lam` (ExamAnswer)
  - MCQ: Lưu `selectedOption` (QuestionOption)
  - FILL: Lưu `fillAnswer` (String)
  - Auto-save: Frontend gọi API này mỗi khi sinh viên chọn đáp án (debounce 500ms)

### Chức năng 8.3: Nộp bài
- **API:** `POST /api/exam-attempts/{attemptId}/submit`
- **Phương thức:** `ExamAttemptService.submitAttempt(Long attemptId, User student)`
- **Phân quyền:** STUDENT
- **Mô tả:**
  - Kiểm tra thời gian (nếu quá thời gian → auto-submit)
  - Chấm điểm tự động:
    - MCQ: So sánh `selectedOption.correct == true`
    - FILL: So sánh `fillAnswer` với `QuestionAnswer.correctAnswer` (normalize: trim, lowercase, remove diacritics)
  - Cập nhật status = SUBMITTED
  - Tính điểm: `marksPerQuestion * số câu đúng`
  - Trả về: `SubmitAttemptResponse` (score, totalMarks)

### Chức năng 8.4: Xem lịch sử làm bài
- **API:** `GET /api/exam-attempts/history`
- **Phương thức:** `ExamAttemptService.getStudentHistory(Long studentId)`
- **Phân quyền:** STUDENT
- **Trả về:** `List<ExamAttemptResponse>`

### Chức năng 8.5: Xem chi tiết bài làm
- **API:** `GET /api/exam-attempts/{attemptId}`
- **Phương thức:** `ExamAttemptService.getAttemptDetail(Long attemptId, User user)`
- **Phân quyền:** Tất cả (kiểm tra quyền trong service)
- **Mô tả:**
  - Sinh viên: Chỉ xem được bài của mình
  - Giáo viên/Admin: Xem được tất cả
  - Trả về: `ExamAttemptDetailResponse` (questions với đáp án đúng, đáp án sinh viên, điểm)

### Chức năng 8.6: Báo cáo vi phạm
- **API:** `POST /api/exam-attempts/{attemptId}/violations`
- **Phương thức:** `ViolationService.reportViolation(Long attemptId, User student, ViolationRequest request)`
- **Phân quyền:** STUDENT
- **Mô tả:** 
  - Frontend tự động phát hiện: tab switch, copy/paste, right-click, window blur
  - Gửi báo cáo về backend
  - Lưu vào bảng `vi_pham` (ExamViolation)

---

## 9. GIÁM SÁT THI

### Controller: `SupervisorController`
**Path:** `/api/supervisor`

### Chức năng 9.1: Xem danh sách kỳ thi được phân công
- **API:** `GET /api/supervisor/exams`
- **Phương thức:** `ExamInstanceService.getInstanceDto(Long examInstanceId)` (qua ExamSupervisorRepository)
- **Phân quyền:** SUPERVISOR
- **Trả về:** `List<ExamInstanceResponse>`

### Chức năng 9.2: Xem danh sách bài làm
- **API:** `GET /api/supervisor/exams/attempts`
- **Phương thức:** `ExamAttemptService.getAttemptsForInstance(Long examInstanceId)`
- **Phân quyền:** SUPERVISOR
- **Trả về:** `List<ExamAttemptResponse>`

### Chức năng 9.3: Xem thống kê
- **API:** `GET /api/supervisor/statistics`
- **Phương thức:** `SupervisorService.getStatistics(User supervisor)`
- **Phân quyền:** SUPERVISOR
- **Trả về:** `SupervisorStatisticsResponse`

### Chức năng 9.4: Xem vi phạm
- **API:** `GET /api/exam-attempts/exam/{examInstanceId}/violations`
- **Phương thức:** `ViolationService.getViolationsForExam(Long examInstanceId)`
- **Phân quyền:** ADMIN, TEACHER, SUPERVISOR
- **Trả về:** `List<ViolationResponse>`

---

## 10. THỐNG KÊ

### Controller: `StatisticsController`
**Path:** `/api/statistics`

### Chức năng 10.1: Thống kê kỳ thi
- **API:** `GET /api/statistics/exam/{examInstanceId}`
- **Phương thức:** `StatisticsService.getExamStatistics(Long examInstanceId)`
- **Phân quyền:** ADMIN, TEACHER, SUPERVISOR
- **Trả về:** `ExamStatisticsResponse`
- **Mô tả:** Thống kê điểm số, số lượng học sinh làm bài, tỷ lệ đỗ/trượt

---

## 🔑 CÁC PHƯƠNG THỨC QUAN TRỌNG TRONG SERVICE

### ExamTemplateService
- `createTemplate()`: Tạo khung đề, validate structures
- `updateTemplate()`: Cập nhật, xóa và tạo lại structures
- `validateStructure()`: Kiểm tra tổng số câu = totalQuestions

### ExamInstanceService
- `createInstance()`: Tạo kỳ thi, generate questions từ template
- `generateExamQuestions()`: Random câu hỏi từ template structure
- `updateInstance()`: Cập nhật, kiểm tra đã có học sinh làm bài chưa

### ExamAttemptService
- `startAttempt()`: Bắt đầu làm bài, shuffle questions/options
- `answerQuestion()`: Lưu câu trả lời
- `submitAttempt()`: Nộp bài, chấm điểm tự động
- `buildQuestionView()`: Build danh sách câu hỏi với shuffle
- `toQuestionView()`: Build từng câu hỏi với shuffle options
- `gradeAttempt()`: Chấm điểm (MCQ: so sánh correct, FILL: normalize và so sánh)

### QuestionService
- `createQuestion()`: Tạo câu hỏi, validate options/answers
- `bulkCreateQuestions()`: Import hàng loạt, kiểm tra trùng
- `buildQuestionEntity()`: Build entity từ request
- `validateMcqOptions()`: Kiểm tra có ít nhất 1 option đúng
- `validateFillAnswers()`: Kiểm tra có ít nhất 1 đáp án

### StudentGroupService
- `assignStudents()`: Gán sinh viên (xóa cũ, tạo mới)
- `getStudentsInGroup()`: Lấy danh sách sinh viên trong nhóm

---

## 📝 LƯU Ý KHI TRẢ LỜI THẦY

### Khi thầy hỏi "Chức năng X được implement bằng phương thức nào?"
1. **Xác định Controller:** Tìm controller có path liên quan
2. **Xác định API endpoint:** Xem @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
3. **Xác định Service method:** Controller gọi service method nào
4. **Giải thích logic:** Service method làm gì, validate gì, lưu vào bảng nào

### Ví dụ:
**"Chức năng tạo câu hỏi được implement như thế nào?"**
- Controller: `QuestionController`
- API: `POST /api/questions`
- Service: `QuestionService.createQuestion()`
- Logic:
  1. Validate: MCQ phải có option đúng, FILL phải có đáp án
  2. Build entity từ request
  3. Lưu vào bảng `cau_hoi`, `lua_chon` (nếu MCQ), `dap_an_cau_hoi` (nếu FILL)
  4. Trả về QuestionResponse

**"Chức năng shuffle câu hỏi được implement như thế nào?"**
- Service: `ExamAttemptService.buildQuestionView()`
- Logic:
  1. Lấy danh sách câu hỏi từ template
  2. Nếu `shuffleQuestions = true` → shuffle với seed = attemptId * 31 + studentId * 17
  3. Với mỗi câu hỏi, nếu `shuffleOptions = true` → shuffle options với seed = attemptId * 31 + studentId * 17 + questionId * 7
  4. Mỗi sinh viên có thứ tự câu hỏi và đáp án khác nhau

**"Chức năng chấm điểm tự động được implement như thế nào?"**
- Service: `ExamAttemptService.submitAttempt()` → `gradeAttempt()`
- Logic:
  1. Với mỗi câu trả lời:
     - MCQ: Kiểm tra `selectedOption.correct == true`
     - FILL: Normalize đáp án (trim, lowercase, remove diacritics) rồi so sánh với `QuestionAnswer.correctAnswer`
  2. Tính điểm: `marksPerQuestion * số câu đúng`
  3. Cập nhật status = SUBMITTED

