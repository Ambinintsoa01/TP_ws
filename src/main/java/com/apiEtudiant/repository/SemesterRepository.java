package com.apiEtudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.apiEtudiant.entity.Semester;
import java.util.Optional;
import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByLabel(String label);

    // Fetch semesters with their level to avoid lazy initialization outside transaction
    @Query("SELECT s FROM Semester s LEFT JOIN FETCH s.level")
    List<Semester> findAllWithLevel();
}