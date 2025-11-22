package com.apiEtudiant.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.apiEtudiant.entity.OptionalGroupSubject;

public interface OptionalGroupSubjectRepository extends JpaRepository<OptionalGroupSubject, Long> {

    @Query("SELECT ogs FROM OptionalGroupSubject ogs JOIN ogs.optionalGroup og JOIN og.semester s WHERE s.id = :semesterId")
    List<OptionalGroupSubject> findByOptionalGroup_Semester_Id(@Param("semesterId") Long semesterId);

}
