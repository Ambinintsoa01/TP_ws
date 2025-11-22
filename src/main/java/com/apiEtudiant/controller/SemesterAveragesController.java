package com.apiEtudiant.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apiEtudiant.repository.GradeRepository;

@RestController
@RequestMapping("/api/semester-averages")
public class SemesterAveragesController {

    private final GradeRepository gradeRepository;

    public SemesterAveragesController(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    @GetMapping("/{label}")
    public List<Map<String, Object>> getAverages(@PathVariable("label") String label) {
        List<Object[]> rows = gradeRepository.findStudentAveragesBySemesterLabel(label);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long studentId = r[0] != null ? ((Number) r[0]).longValue() : null;
            String firstName = r[1] != null ? r[1].toString() : "";
            String lastName = r[2] != null ? r[2].toString() : "";
            String semesterLabel = r[3] != null ? r[3].toString() : null;
            java.time.LocalDate startDate = r[4] != null ? (java.time.LocalDate) r[4] : null;
            Double avg = r[5] != null ? ((Number) r[5]).doubleValue() : null;
            long belowSix = r[6] != null ? ((Number) r[6]).longValue() : 0L;
            long belowTen = r[7] != null ? ((Number) r[7]).longValue() : 0L;

            double roundedAvg = avg != null ? Math.round(avg * 100.0) / 100.0 : Double.NaN;
            String decision = computeDecisionLabel(roundedAvg, belowSix, belowTen);

            Map<String, Object> map = new HashMap<>();
            map.put("studentId", studentId);
            map.put("name", (lastName + " " + firstName).trim());
            map.put("semester", semesterLabel);
            map.put("year", startDate != null ? startDate.getYear() : null);
            map.put("average", !Double.isNaN(roundedAvg) ? roundedAvg : null);
            map.put("decision", decision);
            map.put("belowSix", belowSix);
            map.put("belowTen", belowTen);
            result.add(map);
        }
        return result;
    }

    // Keep compatibility with frontend path /api/semesters/{label}/averages
    @GetMapping("/api/semesters/{label}/averages")
    public List<Map<String, Object>> getAveragesAlias(@PathVariable("label") String label) {
        return getAverages(label);
    }

    private String computeDecisionLabel(Double average, long belowSix, long belowTen) {
        if (average == null) return "—";
        if (average < 10 || belowSix > 0 || belowTen >= 3) return "Ajourné";
        return "Admis";
    }
}
