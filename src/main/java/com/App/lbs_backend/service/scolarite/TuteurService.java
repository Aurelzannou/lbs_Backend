package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.auth.AuthResponse;
import com.App.lbs_backend.dto.auth.LoginRequest;
import com.App.lbs_backend.dto.request.TuteurRequest;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.TuteurMapper;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Service
public class TuteurService extends AbstractBaseService<Tuteur, TuteurResponse> {

    private final TuteurRepository tuteurRepository;
    private final TuteurMapper tuteurMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public TuteurService(TuteurRepository tuteurRepository, TuteurMapper tuteurMapper, 
                         PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        super(Tuteur.class);
        this.tuteurRepository = tuteurRepository;
        this.tuteurMapper = tuteurMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public BaseRepository<Tuteur> repository() {
        return tuteurRepository;
    }

    @Override
    public Mapper<Tuteur, TuteurResponse> mapper() {
        return tuteurMapper;
    }

    @Transactional
    public TuteurResponse register(TuteurRequest request) {
        if (tuteurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un tuteur avec cet email existe déjà");
        }

        Tuteur tuteur = new Tuteur();
        tuteur.setNom(request.getNom());
        tuteur.setPrenom(request.getPrenom());
        tuteur.setEmail(request.getEmail());
        tuteur.setTelephone1(request.getTelephone1());
        tuteur.setTelephone2(request.getTelephone2());
        tuteur.setProfession(request.getProfession());
        tuteur.setAdresse(request.getAdresse());
        tuteur.setCode(request.getCode());
        tuteur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        tuteur.setActif(true);

        return mapper().toResponse(tuteurRepository.save(tuteur));
    }

    public AuthResponse login(LoginRequest request) {
        Tuteur tuteur = tuteurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), tuteur.getMotDePasse())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        if (tuteur.getActif() != null && !tuteur.getActif()) {
            throw new BadCredentialsException("Ce compte est désactivé");
        }

        String token = jwtTokenProvider.generateToken(tuteur.getEmail(), "TUTEUR");

        return AuthResponse.builder()
                .token(token)
                .email(tuteur.getEmail())
                .nom(tuteur.getNom())
                .prenom(tuteur.getPrenom())
                .uuid(tuteur.getUuid())
                .build();
    }
}
