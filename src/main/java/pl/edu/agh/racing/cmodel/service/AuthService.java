package pl.edu.agh.racing.cmodel.service;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.agh.racing.cmodel.dto.NotificationEmailDto;
import pl.edu.agh.racing.cmodel.dto.request.LoginRequest;
import pl.edu.agh.racing.cmodel.dto.request.RegisterRequest;
import pl.edu.agh.racing.cmodel.dto.response.AuthenticationResponse;
import pl.edu.agh.racing.cmodel.exception.CModelException;
import pl.edu.agh.racing.cmodel.model.ERole;
import pl.edu.agh.racing.cmodel.model.Role;
import pl.edu.agh.racing.cmodel.model.User;
import pl.edu.agh.racing.cmodel.model.VerificationToken;
import pl.edu.agh.racing.cmodel.repository.RoleRepository;
import pl.edu.agh.racing.cmodel.repository.UserRepository;
import pl.edu.agh.racing.cmodel.repository.VerificationTokenRepository;
import pl.edu.agh.racing.cmodel.security.jwt.JwtProvider;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RoleRepository roleRepository;

    @Transactional
    public void signup(RegisterRequest registerRequest) {
        if (!isMailOfRacingTeam(registerRequest.getEmail())) {
            throw new CModelException("Email doesn't belong to the AGH racing team '@racing.agh.edu.pl'");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .surname(registerRequest.getSurname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(getInitialSetOfRoles())
                .build();

        userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmailDto("Please Activate Your Account",
                user.getEmail(), "http://localhost:8081/api/auth/accountVerification/" + token));
    }

    private boolean isMailOfRacingTeam(String email) {
        return email.contains("@racing.agh.edu.pl");
    }

    private Set<Role> getInitialSetOfRoles() {
        return Set.of(roleRepository.findRoleByRole(ERole.ROLE_NEWBIE)
                .orElse(roleRepository.save(Role.builder().role(ERole.ROLE_NEWBIE).build())));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(Duration.ofDays(7)))
                .build();
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(() -> new CModelException("Invalid verification token"));
        fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    void fetchUserAndEnable(VerificationToken verificationToken) {
        String email = verificationToken.getUser().getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CModelException("User not found with email: " + email));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateJwtToken(authenticate);
        return new AuthenticationResponse(loginRequest.getEmail(),  token);
    }
}
