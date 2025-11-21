package com.apiEtudiant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subject_transfers")
public class SubjectTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subject", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_source_semester", nullable = false)
    private Semester sourceSemester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_target_semester", nullable = false)
    private Semester targetSemester;

    public SubjectTransfer() {}

    public SubjectTransfer(Long id, Subject subject, Semester sourceSemester, Semester targetSemester) {
        this.id = id;
        this.subject = subject;
        this.sourceSemester = sourceSemester;
        this.targetSemester = targetSemester;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Semester getSourceSemester() { return sourceSemester; }
    public void setSourceSemester(Semester sourceSemester) { this.sourceSemester = sourceSemester; }
    public Semester getTargetSemester() { return targetSemester; }
    public void setTargetSemester(Semester targetSemester) { this.targetSemester = targetSemester; }
}