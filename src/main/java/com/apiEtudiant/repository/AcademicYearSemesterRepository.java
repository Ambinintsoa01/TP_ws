package com.apiEtudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apiEtudiant.entity.AcademicYearSemester;

public interface AcademicYearSemesterRepository extends JpaRepository<AcademicYearSemester, Long> {
}