package integration;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class GettingChallengeParticipatorsIntegrationTest extends EmTestsBase {

    private final ChallengesRepository challengesRepository = new ChallengesRepository();
    private final UsersRepository usersRepository = new UsersRepository();

    private Challenge challenge;

    @Before
    public void setUp() {
        openTransaction();
        User creator = usersRepository.createUser("creator");
        challenge = challengesRepository.createChallenge(new Challenge(creator, "challengeName", ChallengeCategory.ALL, "videoId", true));
        closeTransaction();
    }

    @After
    public void tearDown() {
        openTransaction();
        JPA.em().createQuery("DELETE FROM User").executeUpdate();
        JPA.em().createQuery("DELETE FROM Challenge").executeUpdate();
        JPA.em().createQuery("DELETE FROM ChallengeParticipation ").executeUpdate();
        closeTransaction();
    }

    @Test
    public void shouldFindParticipatorIfOneParticipationInChallenge() throws Exception {
        openTransaction();
        User participator = usersRepository.createUser("participator");
        challengesRepository.persistChallengeParticipation(new ChallengeParticipation(challenge, participator));
        closeTransaction();

        openTransaction();
        List<User> allParticipators = challengesRepository.getAllParticipatorsOf(challenge);
        closeTransaction();

        assertEquals(1, allParticipators.size());
        assertEquals(participator, allParticipators.get(0));
    }

    @Test
    public void shouldFindBothParticipatorsIfTwoParticipationInChallenge() throws Exception {
        openTransaction();
        User participatorOne = usersRepository.createUser("participatorOne");
        challengesRepository.persistChallengeParticipation(new ChallengeParticipation(challenge, participatorOne));
        User participatorTwo = usersRepository.createUser("participatorTwo");
        challengesRepository.persistChallengeParticipation(new ChallengeParticipation(challenge, participatorTwo));
        closeTransaction();

        openTransaction();
        List<User> allParticipators = challengesRepository.getAllParticipatorsOf(challenge);
        closeTransaction();

        assertEquals(2, allParticipators.size());
        assertTrue(allParticipators.contains(participatorOne));
        assertTrue(allParticipators.contains(participatorTwo));
    }

    @Test
    public void shouldFindNoParticipatorsIfNoParticipationsInChallenge() throws Exception {
        openTransaction();
        List<User> allParticipators = challengesRepository.getAllParticipatorsOf(challenge);
        closeTransaction();

        assertTrue(allParticipators.isEmpty());
    }


}
