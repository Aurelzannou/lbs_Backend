package com.App.lbs_backend.config;

import com.App.lbs_backend.core.utils.AppUtil;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Fournit l'utilisateur connecté (via JWT Keycloak) à JPA Auditing.
 * Renseigne automatiquement le champ modifierPar de toutes les entités.
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return AppUtil.connectedUser();
    }
}
