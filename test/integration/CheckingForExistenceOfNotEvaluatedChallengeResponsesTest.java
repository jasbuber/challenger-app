package integration;

import domain.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class CheckingForExistenceOfNotEvaluatedChallengeResponsesTest extends EmTestsBase {

    private final ChallengesRepository challengesRepository = new ChallengesRepository();
    private final UsersRepository usersRepository = new UsersRepository();

    private ChallengeParticipation challengeParticipation;

    @Before
    public void setUp() {
        createChallengeParticipation();
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void shouldFindNotEvaluatedChallengeResponse() throws Exception {
        submitChallengeResponse(challengeParticipation);
        assertTrue(isNotEvaluatedChallengeResponseExists());
    }


    @Test
    public void shouldNotFindNotEvaluatedChallengeResponseIfChallengeResponseAccepted() throws Exception {
        ChallengeResponse challengeResponse = submitChallengeResponse(challengeParticipation);
        acceptChallengeResponse(challengeResponse);
        assertFalse(isNotEvaluatedChallengeResponseExists());
    }

    @Test
    public void shouldNotFindNotEvaluatedChallengeResponseIfChallengeResponseRefused() throws Exception {
        ChallengeResponse challengeResponse = submitChallengeResponse(challengeParticipation);
        refuseChallengeResponse(challengeResponse);
        assertFalse(isNotEvaluatedChallengeResponseExists());
    }

    @Test
    public void shouldNotFindNotEvaluatedChallengeResponseIfNoChallengeResponses() throws Exception {
        assertFalse(isNotEvaluatedChallengeResponseExists());
    }


    private void createChallengeParticipation() {
        openTransaction();
        User creator = usersRepository.createUser("creator");
        Challenge challenge = challengesRepository.createChallenge(new Challenge(creator, "challenge", ChallengeCategory.ALL, "videoId", true, 0));

        User participator = usersRepository.createUser("participator");
        challengeParticipation = challengesRepository.persistChallengeParticipation(new ChallengeParticipation(challenge, participator));
        closeTransaction();
    }

    private ChallengeResponse submitChallengeResponse(ChallengeParticipation challengeParticipation) {
        openTransaction();
        ChallengeResponse challengeResponse = challengesRepository.addChallengeResponse(new ChallengeResponse(challengeParticipation));
        closeTransaction();

        return challengeResponse;
    }

    private boolean isNotEvaluatedChallengeResponseExists() {
        openTransaction();
        boolean isNotScoredChallengeResponseExists = challengesRepository.isNotEvaluatedChallengeResponseExistsFor(challengeParticipation);
        closeTransaction();
        return isNotScoredChallengeResponseExists;
    }

    private void acceptChallengeResponse(ChallengeResponse challengeResponse) {
        openTransaction();
        challengeResponse.accept();
        challengesRepository.updateChallengeResponse(challengeResponse);
        closeTransaction();
    }

    private void refuseChallengeResponse(ChallengeResponse challengeResponse) {
        openTransaction();
        challengeResponse.refuse();
        challengesRepository.updateChallengeResponse(challengeResponse);
        closeTransaction();
    }
}
