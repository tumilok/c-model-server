package pl.edu.agh.racing.cmodel.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class VerificationTokenRepositoryTest {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    // TODO: Fix VerificationTokenRepositoryTest

//    @Test
//    public void shouldSaveVerificationToken() {
//        // given
//        Role role = new Role(null, ERole.ROLE_NEWBIE, new HashSet<>());
//        User user = new User(null, "John", "Peterson",
//                "john.peterson@gmail.com", "12345678", false, Set.of(role), new HashSet<>(), null);
//
//        VerificationToken verificationToken1 = new VerificationToken(null, "some token", user, Instant.now());
//        VerificationToken verificationToken2 = new VerificationToken(null, "some other token", user, Instant.now());
//
//        // when
//        VerificationToken savedToken1 = verificationTokenRepository.save(verificationToken1);
//        VerificationToken savedToken2 = verificationTokenRepository.save(verificationToken2);
//
//        // then
//        assertThat(savedToken1).usingRecursiveComparison().isEqualTo(verificationToken1);
//        assertThat(savedToken2).usingRecursiveComparison().isEqualTo(verificationToken2);
//    }
}
