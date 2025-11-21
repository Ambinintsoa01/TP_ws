package com.apiEtudiant.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.apiEtudiant.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s WHERE s.label = :label")
    List<Grade> findBySemesterLabel(@Param("label") String label);

    @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s JOIN s.level l WHERE l.label = :levelLabel")
    List<Grade> findByLevelLabel(@Param("levelLabel") String levelLabel);

    @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s WHERE g.student.id = :studentId AND s.label = :label")
    List<Grade> findByStudentIdAndSemesterLabel(@Param("studentId") Long studentId, @Param("label") String label);

    @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s JOIN s.level l WHERE g.student.id = :studentId AND l.label = :levelLabel")
    List<Grade> findByStudentIdAndLevelLabel(@Param("studentId") Long studentId, @Param("levelLabel") String levelLabel);
}