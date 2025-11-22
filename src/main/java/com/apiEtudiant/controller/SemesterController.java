package com.apiEtudiant.controller;

import java.util.List;
// import java.util.Map; (removed duplicate)
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apiEtudiant.repository.SemesterRepository;
import com.apiEtudiant.repository.GradeRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterRepository semesterRepository;
    private final GradeRepository gradeRepository;

    public SemesterController(SemesterRepository semesterRepository, GradeRepository gradeRepository) {
        this.semesterRepository = semesterRepository;
        this.gradeRepository = gradeRepository;
    }

        @GetMapping
            public List<?> getAllSemesters() {
            // Use repository method that fetches level to avoid lazy-loading errors
            return semesterRepository.findAllWithLevel().stream()
                .map(s -> Map.of(
                    "id", s.getId(),
                    "label", s.getLabel(),
                    "level", s.getLevel() != null ? s.getLevel().getLabel() : null))
                .collect(Collectors.toList());
                }

        @GetMapping("/{label}/averages")
        public List<Map<String, Object>> getAveragesBySemester(@PathVariable("label") String label) {
            List<Object[]> rows = gradeRepository.findStudentAveragesBySemesterLabel(label);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object[] r : rows) {
                Long studentId = r[0] != null ? ((Number) r[0]).longValue() : null;
                String firstName = r[1] != null ? r[1].toString() : "";
                String lastName = r[2] != null ? r[2].toString() : "";
                String semesterLabel = r[3] != null ? r[3].toString() : null;
                java.time.LocalDate startDate = r[4] != null ? (java.time.LocalDate) r[4] : null;
                Double avg = r[5] != null ? ((Number) r[5]).doubleValue() : null;

                Map<String, Object> map = new HashMap<>();
                map.put("studentId", studentId);
                map.put("name", lastName + " " + firstName);
                map.put("semester", semesterLabel);
                map.put("year", startDate != null ? startDate.getYear() : null);
                map.put("average", avg != null ? Math.round(avg * 100.0) / 100.0 : null);
                result.add(map);
            }
            return result;
        }
}
