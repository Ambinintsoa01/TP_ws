package com.apiEtudiant.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.apiEtudiant.entity.Grade;
import com.apiEtudiant.repository.GradeRepository;

@Service
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public List<Grade> getGradesBySemesterLabel(String semesterLabel) {
        return gradeRepository.findBySemesterLabel(semesterLabel);
    }

    public List<Grade> getGradesByLevelLabel(String levelLabel) {
        return gradeRepository.findByLevelLabel(levelLabel);
    }

    public List<Grade> getGradesByStudentAndSemesterLabel(Long studentId, String semesterLabel) {
        return gradeRepository.findByStudentIdAndSemesterLabel(studentId, semesterLabel);
    }

    public List<Grade> getGradesByStudentAndLevelLabel(Long studentId, String levelLabel) {
        return gradeRepository.findByStudentIdAndLevelLabel(studentId, levelLabel);
    }
}