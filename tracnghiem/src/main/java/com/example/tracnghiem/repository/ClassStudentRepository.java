package com.example.tracnghiem.repository;

import com.example.tracnghiem.domain.group.ClassStudent;
import com.example.tracnghiem.domain.group.ClassStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, ClassStudentId> {
    List<ClassStudent> findByStudentGroup_Id(Long studentGroupId);

    List<ClassStudent> findByStudent_Id(Long studentId);

    @Query("SELECT cs.studentGroup.id, COUNT(cs) FROM ClassStudent cs GROUP BY cs.studentGroup.id")
    List<Object[]> countStudentsByGroup();
}

