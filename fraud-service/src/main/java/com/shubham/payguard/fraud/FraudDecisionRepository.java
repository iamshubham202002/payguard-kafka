package com.shubham.payguard.fraud;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudDecisionRepository extends JpaRepository<FraudDecisionEntity, UUID> {
}
