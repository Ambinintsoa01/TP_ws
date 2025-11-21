package com.apiEtudiant.dto;

import java.util.List;

public class SemesterNotesResponse {

    private final Long studentId;
    private final String semesterCode;
    private final String semesterLabel;
    private final List<GradeDTO> notes;

    public SemesterNotesResponse(Long studentId, String semesterCode, String semesterLabel, List<GradeDTO> notes) {
        this.studentId = studentId;
        this.semesterCode = semesterCode;
        this.semesterLabel = semesterLabel;
        this.notes = notes;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getSemesterCode() {
        return semesterCode;
    }

    public String getSemesterLabel() {
        return semesterLabel;
    }

    public List<GradeDTO> getNotes() {
        return notes;
    }
}
