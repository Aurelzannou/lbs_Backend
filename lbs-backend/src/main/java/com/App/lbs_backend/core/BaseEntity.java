package com.App.lbs_backend.core;

/**
 * Interface de base pour les entités devant être exposées via le MasterController.
 * Force la présence d'un UUID commun pour les recherches/suppressions par URL.
 */
public interface BaseEntity {
    String getUuid();
}
