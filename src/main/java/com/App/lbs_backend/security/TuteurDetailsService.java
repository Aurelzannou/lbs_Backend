package com.App.lbs_backend.security;

import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.TuteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TuteurDetailsService implements UserDetailsService {

    private final TuteurRepository tuteurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Tuteur tuteur = tuteurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Tuteur not found with email: " + email));
        return new TuteurPrincipal(tuteur);
    }
}
