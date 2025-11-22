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

        @Query("SELECT g.student.id, g.student.firstName, g.student.lastName, s.label, ays.startDate, AVG(g.grade), " +
            "SUM(CASE WHEN g.grade < 6 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN g.grade < 10 THEN 1 ELSE 0 END) " +
            "FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s " +
            "WHERE s.label = :label " +
            "GROUP BY g.student.id, g.student.firstName, g.student.lastName, s.label, ays.startDate " +
            "ORDER BY AVG(g.grade) DESC")
        List<Object[]> findStudentAveragesBySemesterLabel(@Param("label") String label);

    @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s WHERE g.student.id = :studentId AND s.label = :label")
    List<Grade> findByStudentIdAndSemesterLabel(@Param("studentId") Long studentId, @Param("label") String label);

    @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s JOIN s.level l WHERE g.student.id = :studentId AND l.label = :levelLabel")
    List<Grade> findByStudentIdAndLevelLabel(@Param("studentId") Long studentId, @Param("levelLabel") String levelLabel);

    @Query("SELECT s.label, AVG(g.grade) " +
            "FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester s " +
            "WHERE g.student.id = :studentId AND s.label IN :labels " +
            "GROUP BY s.label")
    List<Object[]> findAveragesByStudentAndSemesters(@Param("studentId") Long studentId, @Param("labels") List<String> labels);

            @Query("SELECT s.id, s.lastName, s.firstName, p.label, g.id, subj.id, subj.code, subj.title, g.grade, ays.semester.id, ays.option.label " +
                "FROM Grade g JOIN g.student s JOIN s.program p JOIN g.subject subj JOIN g.academicYearSemester ays " +
                "WHERE ays.semester.id = :semesterId " +
                "ORDER BY s.id, subj.id")
            List<Object[]> findGradesBySemesterId(@Param("semesterId") Long semesterId);

                @Query("SELECT g FROM Grade g JOIN g.academicYearSemester ays JOIN ays.semester sem WHERE sem.id = :semesterId AND g.subject.id IN :subjectIds")
                List<com.apiEtudiant.entity.Grade> findBySemesterIdAndSubjectIds(@Param("semesterId") Long semesterId, @Param("subjectIds") java.util.List<Long> subjectIds);
}