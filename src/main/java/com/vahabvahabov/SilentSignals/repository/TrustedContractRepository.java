package com.vahabvahabov.SilentSignals.repository;

import com.vahabvahabov.SilentSignals.model.contact.TrustedContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedContractRepository extends JpaRepository<TrustedContact, Long> {

    List<TrustedContact> findByUserId(Long userId);

    @Query("SELECT tc FROM TrustedContact tc WHERE tc.user.id = :userId AND tc.email = :email")
    Optional<TrustedContact> findByUserIdAndEmail(@Param("userId") Long userId, @Param("email") String email);
}