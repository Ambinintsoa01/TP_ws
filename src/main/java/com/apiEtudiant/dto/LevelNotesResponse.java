package com.apiEtudiant.dto;

import java.util.List;

public class LevelNotesResponse {

    private final Long studentId;
    private final String levelLabel;
    private final List<SemesterNotesBlock> semesters;

    public LevelNotesResponse(Long studentId, String levelLabel, List<SemesterNotesBlock> semesters) {
        this.studentId = studentId;
        this.levelLabel = levelLabel;
        this.semesters = semesters;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getLevelLabel() {
        return levelLabel;
    }

    public List<SemesterNotesBlock> getSemesters() {
        return semesters;
    }
}
