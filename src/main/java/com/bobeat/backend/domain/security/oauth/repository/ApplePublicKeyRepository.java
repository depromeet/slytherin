package com.bobeat.backend.domain.security.oauth.repository;

import com.bobeat.backend.domain.security.oauth.domain.ApplePublicKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplePublicKeyRepository extends JpaRepository<ApplePublicKey, Long> {
}
