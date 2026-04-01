package com.App.lbs_backend.core;

import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Classe abstraite commune à toutes les entités auditables.
 * Gère automatiquement modifierLe et modifierPar via JPA Auditing.
 * Chaque sous-entité doit surcharger les noms de colonnes via @AttributeOverrides.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @LastModifiedDate
    private LocalDateTime modifierLe;

    @LastModifiedBy
    @Column(length = 100)
    private String modifierPar;

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }
}

