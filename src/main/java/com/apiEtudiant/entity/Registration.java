package com.apiEtudiant.entity;

import java.time.LocalDate;
import java.math.BigDecimal;
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
@Table(name = "registrations")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", nullable = false, unique = true)
    private BigDecimal registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_student", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_academic_year_semester", nullable = false)
    private AcademicYearSemester academicYearSemester;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    public Registration() {
    }

    public Registration(Long id, BigDecimal registrationNumber, Student student,
            AcademicYearSemester academicYearSemester, LocalDate registrationDate) {
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.student = student;
        this.academicYearSemester = academicYearSemester;
        this.registrationDate = registrationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(BigDecimal registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public AcademicYearSemester getAcademicYearSemester() {
        return academicYearSemester;
    }

    public void setAcademicYearSemester(AcademicYearSemester academicYearSemester) {
        this.academicYearSemester = academicYearSemester;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}