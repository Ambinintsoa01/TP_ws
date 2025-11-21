package com.apiEtudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apiEtudiant.entity.Level;
import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findByLabel(String label);
}