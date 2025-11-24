package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.group.ClassStudent;
import com.example.tracnghiem.domain.group.ClassStudentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, ClassStudentId> {
    List<ClassStudent> findByStudentGroup_Id(Long studentGroupId);

    List<ClassStudent> findByStudent_Id(Long studentId);
}

