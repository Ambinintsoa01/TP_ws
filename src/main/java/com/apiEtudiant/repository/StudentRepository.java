package com.apiEtudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apiEtudiant.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}