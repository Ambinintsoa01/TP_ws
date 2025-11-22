package com.apiEtudiant.dto;

import java.util.List;

public class SemesterNotesBlock {

    private final Long academicYearSemesterId;
    private final String semesterLabel;
    private final List<GradeDTO> notes;

    public SemesterNotesBlock(Long academicYearSemesterId, String semesterLabel, List<GradeDTO> notes) {
        this.academicYearSemesterId = academicYearSemesterId;
        this.semesterLabel = semesterLabel;
        this.notes = notes;
    }

    public Long getAcademicYearSemesterId() {
        return academicYearSemesterId;
    }

    public String getSemesterLabel() {
        return semesterLabel;
    }

    public List<GradeDTO> getNotes() {
        return notes;
    }
}
