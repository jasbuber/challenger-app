package integration;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import integration.EmTestsBase;
import org.junit.After;
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
        User creator = usersRepository.createUser("creator");
        Challenge challenge = challengesRepository.createChallenge(creator, "challengeName", ChallengeCategory.ALL, "videoId", true);
        closeTransaction();

        openTransaction();
        User participator = usersRepository.createUser("participator");
        challengesRepository.createChallengeParticipation(challenge, participator);
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
        User creator = usersRepository.createUser("creator");
        Challenge challenge = challengesRepository.createChallenge(creator, "challengeName", ChallengeCategory.ALL, "videoId", true);
        closeTransaction();

        openTransaction();
        User participatorOne = usersRepository.createUser("participatorOne");
        challengesRepository.createChallengeParticipation(challenge, participatorOne);
        User participatorTwo = usersRepository.createUser("participatorTwo");
        challengesRepository.createChallengeParticipation(challenge, participatorTwo);
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
        User creator = usersRepository.createUser("creator");
        Challenge challenge = challengesRepository.createChallenge(creator, "challengeName", ChallengeCategory.ALL, "videoId", true);
        closeTransaction();

        openTransaction();
        List<User> allParticipators = challengesRepository.getAllParticipatorsOf(challenge);
        closeTransaction();

        assertTrue(allParticipators.isEmpty());
    }


}
