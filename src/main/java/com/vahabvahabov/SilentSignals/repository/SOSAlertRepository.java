package com.vahabvahabov.SilentSignals.repository;

import com.vahabvahabov.SilentSignals.model.alert.AlertStatus;
import com.vahabvahabov.SilentSignals.model.alert.SOSAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SOSAlertRepository extends JpaRepository<SOSAlert, Long> {
    @Query("SELECT sa FROM SOSAlert sa JOIN FETCH sa.user WHERE sa.alertStatus = :alertStatus")
    List<SOSAlert> findByAlertStatus(@Param("alertStatus") AlertStatus alertStatus);
}
