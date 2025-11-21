package com.apiEtudiant.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "honors")
public class Honor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @Column(name = "min_threshold", nullable = false)
    private BigDecimal minThreshold;

    @Column(name = "max_threshold", nullable = false)
    private BigDecimal maxThreshold;

    public Honor() {
    }

    public Honor(Long id, String label, BigDecimal minThreshold, BigDecimal maxThreshold) {
        this.id = id;
        this.label = label;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(BigDecimal minThreshold) {
        this.minThreshold = minThreshold;
    }

    public BigDecimal getMaxThreshold() {
        return maxThreshold;
    }

    public void setMaxThreshold(BigDecimal maxThreshold) {
        this.maxThreshold = maxThreshold;
    }
}