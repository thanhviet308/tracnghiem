package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.group.ClassStudent;
import com.example.tracnghiem.domain.group.ClassStudentId;
import com.example.tracnghiem.domain.group.StudentGroup;
import com.example.tracnghiem.domain.user.User;
import com.example.tracnghiem.domain.user.UserRole;
import com.example.tracnghiem.domain.group.StudentGroupSubject;
import com.example.tracnghiem.domain.group.StudentGroupSubjectId;
import com.example.tracnghiem.domain.subject.Subject;
import com.example.tracnghiem.dto.group.StudentGroupRequest;
import com.example.tracnghiem.dto.group.StudentGroupResponse;
import com.example.tracnghiem.dto.group.StudentGroupSubjectRequest;
import com.example.tracnghiem.dto.group.StudentGroupSubjectResponse;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ClassStudentRepository;
import com.example.tracnghiem.repository.StudentGroupRepository;
import com.example.tracnghiem.repository.StudentGroupSubjectRepository;
import com.example.tracnghiem.repository.SubjectRepository;
import com.example.tracnghiem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;
    private final StudentGroupSubjectRepository studentGroupSubjectRepository;
    private final SubjectRepository subjectRepository;

    public StudentGroupService(StudentGroupRepository studentGroupRepository,
                               ClassStudentRepository classStudentRepository,
                               UserRepository userRepository,
                               StudentGroupSubjectRepository studentGroupSubjectRepository,
                               SubjectRepository subjectRepository) {
        this.studentGroupRepository = studentGroupRepository;
        this.classStudentRepository = classStudentRepository;
        this.userRepository = userRepository;
        this.studentGroupSubjectRepository = studentGroupSubjectRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<StudentGroupResponse> listGroups() {
        List<StudentGroup> groups = studentGroupRepository.findAll();
        
        // Optimize: Get all counts in one query instead of N queries
        Map<Long, Long> studentCountsByGroup = classStudentRepository.countStudentsByGroup().stream()
                .collect(java.util.stream.Collectors.toMap(
                    arr -> (Long) arr[0],
                    arr -> (Long) arr[1]
                ));
        
        return groups.stream()
                .map(group -> toResponse(group, studentCountsByGroup.getOrDefault(group.getId(), 0L).intValue()))
                .toList();
    }

    public StudentGroupResponse createGroup(StudentGroupRequest request) {
        StudentGroup group = StudentGroup.builder()
                .name(request.name())
                .build();
        return toResponse(studentGroupRepository.save(group));
    }

    public StudentGroupResponse updateGroup(Long id, StudentGroupRequest request) {
        StudentGroup group = getGroup(id);
        group.setName(request.name());
        return toResponse(studentGroupRepository.save(group));
    }

    public void deleteGroup(Long id) {
        StudentGroup group = getGroup(id);
        // Xóa mapping sinh viên thuộc nhóm này trước
        List<ClassStudent> mappings = classStudentRepository.findByStudentGroup_Id(id);
        classStudentRepository.deleteAll(mappings);
        studentGroupRepository.delete(group);
    }

    public void assignStudents(Long groupId, List<Long> studentIds) {
        StudentGroup group = getGroup(groupId);
        // Xóa toàn bộ mapping cũ
        List<ClassStudent> existing = classStudentRepository.findByStudentGroup_Id(groupId);
        classStudentRepository.deleteAll(existing);

        // Tạo mapping mới
        for (Long studentId : studentIds) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            if (student.getRole() != UserRole.STUDENT) {
                continue;
            }
            ClassStudent mapping = ClassStudent.builder()
                    .id(new ClassStudentId(groupId, studentId))
                    .studentGroup(group)
                    .student(student)
                    .build();
            classStudentRepository.save(mapping);
        }
    }

    public List<com.example.tracnghiem.dto.user.UserResponse> getStudentsInGroup(Long groupId) {
        getGroup(groupId); // Verify group exists
        List<ClassStudent> classStudents = classStudentRepository.findByStudentGroup_Id(groupId);
        return classStudents.stream()
                .map(cs -> new com.example.tracnghiem.dto.user.UserResponse(
                        cs.getStudent().getId(),
                        cs.getStudent().getFullName(),
                        cs.getStudent().getEmail(),
                        cs.getStudent().getRole(),
                        cs.getStudent().isActive()
                ))
                .toList();
    }

    public List<StudentGroupSubjectResponse> listAssignments() {
        return studentGroupSubjectRepository.findAll().stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    public List<StudentGroupSubjectResponse> getAssignmentsByTeacher(Long teacherId) {
        return studentGroupSubjectRepository.findByTeacher_Id(teacherId).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    public StudentGroupSubjectResponse createAssignment(StudentGroupSubjectRequest request) {
        StudentGroup group = getGroup(request.groupId());
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        User teacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new ResourceNotFoundException("Teacher not found");
        }
        StudentGroupSubjectId id = new StudentGroupSubjectId(request.groupId(), request.subjectId());
        StudentGroupSubject entity = studentGroupSubjectRepository.findById(id)
                .orElseGet(() -> StudentGroupSubject.builder()
                        .id(id)
                        .studentGroup(group)
                        .subject(subject)
                        .teacher(teacher)
                        .build());
        entity.setTeacher(teacher);
        StudentGroupSubject saved = studentGroupSubjectRepository.save(entity);
        return toAssignmentResponse(saved);
    }

    public StudentGroupSubjectResponse updateAssignment(Long groupId, Long subjectId, StudentGroupSubjectRequest request) {
        // Verify the assignment exists
        StudentGroupSubjectId oldId = new StudentGroupSubjectId(groupId, subjectId);
        StudentGroupSubject oldEntity = studentGroupSubjectRepository.findById(oldId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        
        // Check if group or subject changed
        boolean groupChanged = !oldId.getStudentGroupId().equals(request.groupId());
        boolean subjectChanged = !oldId.getSubjectId().equals(request.subjectId());
        
        if (groupChanged || subjectChanged) {
            // If group or subject changed, delete old assignment and create new one
            studentGroupSubjectRepository.deleteById(oldId);
            // Create new assignment with new group/subject
            return createAssignment(request);
        } else {
            // Only teacher changed, update existing assignment
            User teacher = userRepository.findById(request.teacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
            if (teacher.getRole() != UserRole.TEACHER) {
                throw new ResourceNotFoundException("Teacher not found");
            }
            
            oldEntity.setTeacher(teacher);
            StudentGroupSubject saved = studentGroupSubjectRepository.save(oldEntity);
            return toAssignmentResponse(saved);
        }
    }

    public void deleteAssignment(Long groupId, Long subjectId) {
        StudentGroupSubjectId id = new StudentGroupSubjectId(groupId, subjectId);
        studentGroupSubjectRepository.deleteById(id);
    }

    private StudentGroup getGroup(Long id) {
        return studentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student group not found"));
    }

    private StudentGroupResponse toResponse(StudentGroup group) {
        int numberOfStudents = classStudentRepository.findByStudentGroup_Id(group.getId()).size();
        return new StudentGroupResponse(group.getId(), group.getName(), group.getCreatedAt(), numberOfStudents);
    }

    private StudentGroupResponse toResponse(StudentGroup group, int numberOfStudents) {
        return new StudentGroupResponse(group.getId(), group.getName(), group.getCreatedAt(), numberOfStudents);
    }

    private StudentGroupSubjectResponse toAssignmentResponse(StudentGroupSubject entity) {
        String groupName = entity.getStudentGroup() != null ? entity.getStudentGroup().getName() : null;
        String subjectName = entity.getSubject() != null ? entity.getSubject().getName() : null;
        Long teacherId = entity.getTeacher() != null ? entity.getTeacher().getId() : null;
        String teacherName = entity.getTeacher() != null ? entity.getTeacher().getFullName() : null;
        Long subjectId = entity.getId() != null ? entity.getId().getSubjectId() : null;
        Long groupId = entity.getId() != null ? entity.getId().getStudentGroupId() : null;
        return new StudentGroupSubjectResponse(groupId, groupName, subjectId, subjectName, teacherId, teacherName,
                entity.getAssignedAt());
    }
}


