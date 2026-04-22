package com.App.lbs_backend.security;

import com.App.lbs_backend.entity.Tuteur;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class TuteurPrincipal implements UserDetails {

    private final Tuteur tuteur;

    public TuteurPrincipal(Tuteur tuteur) {
        this.tuteur = tuteur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_TUTEUR"));
    }

    @Override
    public String getPassword() {
        return tuteur.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return tuteur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return tuteur.getActif() != null && tuteur.getActif();
    }
}
