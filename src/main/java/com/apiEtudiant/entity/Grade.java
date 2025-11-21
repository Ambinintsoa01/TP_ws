package com.apiEtudiant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_student", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_academic_year_semester", nullable = false)
    private AcademicYearSemester academicYearSemester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subject", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_exam_session", nullable = false)
    private ExamSession examSession;

    @Column(name = "grade", nullable = false)
    private Float grade;

    public Grade() {}

    public Grade(Long id, Student student, AcademicYearSemester academicYearSemester, Subject subject, ExamSession examSession, Float grade) {
        this.id = id;
        this.student = student;
        this.academicYearSemester = academicYearSemester;
        this.subject = subject;
        this.examSession = examSession;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public AcademicYearSemester getAcademicYearSemester() { return academicYearSemester; }
    public void setAcademicYearSemester(AcademicYearSemester academicYearSemester) { this.academicYearSemester = academicYearSemester; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public ExamSession getExamSession() { return examSession; }
    public void setExamSession(ExamSession examSession) { this.examSession = examSession; }
    public Float getGrade() { return grade; }
    public void setGrade(Float grade) { this.grade = grade; }
}