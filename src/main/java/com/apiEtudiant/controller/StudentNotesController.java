package com.apiEtudiant.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

@RestController
@RequestMapping("/api/students")
public class StudentNotesController {

    private final StudentService studentService;
    private final GradeService gradeService;

    public StudentNotesController(StudentService studentService, GradeService gradeService) {
        this.studentService = studentService;
        this.gradeService = gradeService;
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
        Map<String, List<GradeDTO>> grouped = noteDtos.stream()
                .collect(Collectors.groupingBy(dto -> dto.getSemesterLabel() == null ? "UNKNOWN" : dto.getSemesterLabel()));

        List<SemesterNotesBlock> semesters = grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new SemesterNotesBlock(entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(GradeDTO::getSubjectCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        LevelNotesResponse data = new LevelNotesResponse(student.getId(), normalizedLevel, semesters);
        Map<String, Object> meta = Map.of(
                "semesterCount", semesters.size(),
                "notesCount", noteDtos.size());

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
        return normalized;
    }

    private GradeDTO toDto(Grade grade) {
        String semesterLabel = grade.getAcademicYearSemester() != null && grade.getAcademicYearSemester().getSemester() != null
                ? grade.getAcademicYearSemester().getSemester().getLabel()
                : null;
        return new GradeDTO(
                grade.getId(),
                grade.getSubject() != null ? grade.getSubject().getId() : null,
                grade.getSubject() != null ? grade.getSubject().getCode() : null,
                grade.getSubject() != null ? grade.getSubject().getTitle() : null,
                semesterLabel,
                grade.getExamSession() != null ? grade.getExamSession().getSessionDate() : null,
                grade.getGrade());
    }
}
