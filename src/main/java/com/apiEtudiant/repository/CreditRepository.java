package com.apiEtudiant.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.apiEtudiant.entity.Credit;

public interface CreditRepository extends JpaRepository<Credit, Long> {

    @Query("SELECT c FROM Credit c JOIN c.option o WHERE o.id = :optionId")
    List<Credit> findByOptionId(@Param("optionId") Long optionId);
}
