package com.apiEtudiant.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apiEtudiant.dto.ApiResponse;
import com.apiEtudiant.dto.GradeDTO;
import com.apiEtudiant.dto.LevelNotesResponse;
import com.apiEtudiant.dto.SemesterNotesBlock;
import com.apiEtudiant.dto.SemesterNotesResponse;
import com.apiEtudiant.entity.Grade;
import com.apiEtudiant.entity.Student;
import com.apiEtudiant.exception.ApiException;
import com.apiEtudiant.exception.ErrorCode;
import com.apiEtudiant.service.GradeService;
import com.apiEtudiant.service.StudentService;
import com.apiEtudiant.repository.GradeRepository;

@RestController
@RequestMapping("/api/students")
public class StudentNotesController {

    private final StudentService studentService;
    private final GradeService gradeService;
    private final GradeRepository gradeRepository;

    public StudentNotesController(StudentService studentService, GradeService gradeService, GradeRepository gradeRepository) {
        this.studentService = studentService;
        this.gradeService = gradeService;
        this.gradeRepository = gradeRepository;
    }

    @GetMapping("/{studentId}/averages")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getStudentAverages(@PathVariable("studentId") Long studentId) {
        Student student = studentService.requireStudent(studentId);
        List<String> labels = Arrays.asList("S1", "S2", "S3", "S4");
        List<Object[]> rows = gradeRepository.findAveragesByStudentAndSemesters(student.getId(), labels);
        Map<String, Double> map = new LinkedHashMap<>();
        // initialize with nulls to preserve order
        for (String l : labels) map.put(l, null);
        for (Object[] r : rows) {
            String lab = r[0] != null ? r[0].toString() : null;
            Double avg = r[1] != null ? ((Number) r[1]).doubleValue() : null;
            if (avg != null) avg = Math.round(avg * 100.0) / 100.0;
            if (lab != null) map.put(lab, avg);
        }
        return ResponseEntity.ok(ApiResponse.success(map));
    }

    @GetMapping("/{studentId}/notes/semesters/{semesterCode}")
    public ResponseEntity<ApiResponse<SemesterNotesResponse>> getNotesBySemester(
            @PathVariable("studentId") Long studentId,
            @PathVariable("semesterCode") String semesterCode) {

        Student student = studentService.requireStudent(studentId);
        String normalizedSemester = normalizeSemester(semesterCode);

        List<Grade> grades = gradeService.getGradesByStudentAndSemesterLabel(student.getId(), normalizedSemester);
        List<GradeDTO> noteDtos = toSortedDto(grades);
        String semesterLabel = noteDtos.stream().map(GradeDTO::getSemesterLabel).filter(s -> s != null && !s.isBlank())
                .findFirst().orElse(normalizedSemester);

        SemesterNotesResponse data = new SemesterNotesResponse(student.getId(), normalizedSemester, semesterLabel, noteDtos);
        Map<String, Object> meta = Map.of("notesCount", noteDtos.size());
        return ResponseEntity.ok(ApiResponse.success(data, meta));
    }

    @GetMapping("/{studentId}/notes/levels/{levelCode}")
    public ResponseEntity<ApiResponse<LevelNotesResponse>> getNotesByLevel(
            @PathVariable("studentId") Long studentId,
            @PathVariable("levelCode") String levelCode) {

        Student student = studentService.requireStudent(studentId);
        String normalizedLevel = normalizeLevel(levelCode);

        List<GradeDTO> noteDtos = toSortedDto(gradeService.getGradesByStudentAndLevelLabel(student.getId(), normalizedLevel));
        // Group by academic_year_semester id (so S1 and S2 remain separate blocks even if labels are identical)
        Map<String, List<GradeDTO>> grouped = noteDtos.stream()
            .collect(Collectors.groupingBy(dto -> dto.getAcademicYearSemesterId() != null ? dto.getAcademicYearSemesterId().toString()
                : (dto.getSemesterLabel() == null ? "UNKNOWN" : dto.getSemesterLabel())));

        List<SemesterNotesBlock> semesters = grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<GradeDTO> list = entry.getValue().stream()
                    .sorted(Comparator.comparing(GradeDTO::getSubjectCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .collect(Collectors.toList());
                // determine a display label from the first dto
                String label = list.stream().map(GradeDTO::getSemesterLabel).filter(s -> s != null && !s.isBlank()).findFirst().orElse(entry.getKey());
                Long aysId = null;
                try {
                aysId = Long.valueOf(entry.getKey());
                } catch (Exception ex) {
                // leave null if parsing fails
                }
                return new SemesterNotesBlock(aysId, label, list);
            })
            .collect(Collectors.toList());

        LevelNotesResponse data = new LevelNotesResponse(student.getId(), normalizedLevel, semesters);
        // Compute level-average: for each subject keep the most recent grade (by sessionDate) across the whole level
        Map<String, GradeDTO> latestPerSubject = new HashMap<>();
        for (GradeDTO dto : noteDtos) {
            String key = dto.getSubjectId() != null ? dto.getSubjectId().toString()
                    : (dto.getSubjectCode() != null ? dto.getSubjectCode() : String.valueOf(dto.getGradeId()));
            GradeDTO existing = latestPerSubject.get(key);
            if (existing == null) {
                latestPerSubject.put(key, dto);
            } else {
                try {
                    if (dto.getSessionDate() != null && existing.getSessionDate() != null) {
                        if (dto.getSessionDate().isAfter(existing.getSessionDate())) latestPerSubject.put(key, dto);
                    } else if (dto.getSessionDate() != null && existing.getSessionDate() == null) {
                        latestPerSubject.put(key, dto);
                    } else if (dto.getSessionDate() == null && existing.getSessionDate() == null) {
                        if ((dto.getGradeId() != null ? dto.getGradeId() : 0L) > (existing.getGradeId() != null ? existing.getGradeId() : 0L)) {
                            latestPerSubject.put(key, dto);
                        }
                    }
                } catch (Exception ex) {
                    // if any issue, keep existing
                }
            }
        }

        Double levelAverage = null;
        if (!latestPerSubject.isEmpty()) {
            List<Float> grades = latestPerSubject.values().stream().map(GradeDTO::getGrade).filter(g -> g != null).collect(Collectors.toList());
            if (!grades.isEmpty()) {
                double sum = 0.0;
                for (Float f : grades) sum += f;
                levelAverage = Math.round((sum / grades.size()) * 100.0) / 100.0;
            }
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("semesterCount", semesters.size());
        meta.put("notesCount", noteDtos.size());
        if (levelAverage != null) meta.put("levelAverage", levelAverage);

        return ResponseEntity.ok(ApiResponse.success(data, meta));
    }

    private List<GradeDTO> toSortedDto(List<Grade> grades) {
        return grades.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(GradeDTO::getSubjectCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private String normalizeSemester(String semesterCode) {
        if (semesterCode == null) {
            throw new ApiException(ErrorCode.INVALID_SEMESTER, "Le code semestre est obligatoire.", HttpStatus.BAD_REQUEST);
        }
        String normalized = semesterCode.trim().toUpperCase();
        if (!normalized.matches("S[1-4]")) {
            throw new ApiException(ErrorCode.INVALID_SEMESTER,
                    "Le semestre doit être compris entre S1 et S4.", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeLevel(String levelCode) {
        if (levelCode == null) {
            throw new ApiException(ErrorCode.INVALID_LEVEL, "Le code niveau est obligatoire.", HttpStatus.BAD_REQUEST);
        }
        String normalized = levelCode.trim().toUpperCase();
        if (!normalized.matches("L[1-3]")) {
            throw new ApiException(ErrorCode.INVALID_LEVEL,
                    "Le niveau doit être compris entre L1 et L3.", HttpStatus.BAD_REQUEST);
        }
        // Map short codes (L1, L2, L3) to the labels used in the DB (e.g. "Licence 1")
        switch (normalized) {
            case "L1":
                return "Licence 1";
            case "L2":
                return "Licence 2";
            case "L3":
                return "Licence 3";
            default:
                return normalized;
        }
    }

    private GradeDTO toDto(Grade grade) {
        String semesterLabel = grade.getAcademicYearSemester() != null && grade.getAcademicYearSemester().getSemester() != null
            ? grade.getAcademicYearSemester().getSemester().getLabel()
            : null;
        Long aysId = grade.getAcademicYearSemester() != null ? grade.getAcademicYearSemester().getId() : null;
        return new GradeDTO(
            grade.getId(),
            grade.getSubject() != null ? grade.getSubject().getId() : null,
            grade.getSubject() != null ? grade.getSubject().getCode() : null,
            grade.getSubject() != null ? grade.getSubject().getTitle() : null,
            aysId,
            semesterLabel,
            grade.getExamSession() != null ? grade.getExamSession().getSessionDate() : null,
            grade.getGrade());
    }
}
