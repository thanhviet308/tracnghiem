package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.group.StudentGroupSubject;
import com.example.tracnghiem.domain.group.StudentGroupSubjectId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGroupSubjectRepository extends JpaRepository<StudentGroupSubject, StudentGroupSubjectId> {

    List<StudentGroupSubject> findByStudentGroup_Id(Long studentGroupId);

    List<StudentGroupSubject> findBySubject_Id(Long subjectId);
}

