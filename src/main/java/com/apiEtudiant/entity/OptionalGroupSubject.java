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
@Table(name = "optional_group_subjects")
public class OptionalGroupSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subject", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_optional_group", nullable = false)
    private OptionalGroup optionalGroup;

    public OptionalGroupSubject() {
    }

    public OptionalGroupSubject(Long id, Subject subject, OptionalGroup optionalGroup) {
        this.id = id;
        this.subject = subject;
        this.optionalGroup = optionalGroup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public OptionalGroup getOptionalGroup() {
        return optionalGroup;
    }

    public void setOptionalGroup(OptionalGroup optionalGroup) {
        this.optionalGroup = optionalGroup;
    }
}