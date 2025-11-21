package com.apiEtudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apiEtudiant.entity.Semester;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByLabel(String label);
}