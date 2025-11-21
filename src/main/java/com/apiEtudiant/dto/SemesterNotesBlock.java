package com.apiEtudiant.dto;

import java.util.List;

public class SemesterNotesBlock {

    private final String semesterLabel;
    private final List<GradeDTO> notes;

    public SemesterNotesBlock(String semesterLabel, List<GradeDTO> notes) {
        this.semesterLabel = semesterLabel;
        this.notes = notes;
    }

    public String getSemesterLabel() {
        return semesterLabel;
    }

    public List<GradeDTO> getNotes() {
        return notes;
    }
}
