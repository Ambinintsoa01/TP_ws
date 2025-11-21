package com.apiEtudiant.dto;

import java.time.LocalDate;

public class GradeDTO {
    private Long gradeId;
    private Long subjectId;
    private String subjectCode;
    private String subjectTitle;
    private String semesterLabel;
    private LocalDate sessionDate;
    private Float grade;

    public GradeDTO() {}

    public GradeDTO(Long gradeId, Long subjectId, String subjectCode, String subjectTitle, String semesterLabel, LocalDate sessionDate, Float grade) {
        this.gradeId = gradeId;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectTitle = subjectTitle;
        this.semesterLabel = semesterLabel;
        this.sessionDate = sessionDate;
        this.grade = grade;
    }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectTitle() { return subjectTitle; }
    public void setSubjectTitle(String subjectTitle) { this.subjectTitle = subjectTitle; }
    public String getSemesterLabel() { return semesterLabel; }
    public void setSemesterLabel(String semesterLabel) { this.semesterLabel = semesterLabel; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public Float getGrade() { return grade; }
    public void setGrade(Float grade) { this.grade = grade; }
}
