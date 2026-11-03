package com.projects.razorpay.vault_service.repository;

import com.projects.razorpay.vault_service.entity.VaultCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VaultCardRepository extends JpaRepository<VaultCard, UUID> {
}
