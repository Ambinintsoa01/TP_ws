package com.apiEtudiant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "optional_groups")
public class OptionalGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subject_category", nullable = false)
    private SubjectCategory subjectCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_semester", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_option", nullable = false)
    private OptionEntity option;

    public OptionalGroup() {
    }

    public OptionalGroup(Long id, SubjectCategory subjectCategory, Semester semester, OptionEntity option) {
        this.id = id;
        this.subjectCategory = subjectCategory;
        this.semester = semester;
        this.option = option;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubjectCategory getSubjectCategory() {
        return subjectCategory;
    }

    public void setSubjectCategory(SubjectCategory subjectCategory) {
        this.subjectCategory = subjectCategory;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public OptionEntity getOption() {
        return option;
    }

    public void setOption(OptionEntity option) {
        this.option = option;
    }
}