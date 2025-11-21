package com.apiEtudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apiEtudiant.entity.Subject;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
}