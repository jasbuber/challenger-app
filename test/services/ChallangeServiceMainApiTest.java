package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;

import static junit.framework.Assert.assertTrue;

public class ChallangeServiceMainApiTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final ChallengeService challengeService = new ChallengeService(challengesRepository);

    @Test
    public void shouldCreateNewChallengeForUser() throws Exception {
        //when
        Challenge challenge = challengeService.createChallenge(new User("username"));

        //then
        assertTrue(challenge != null);
    }

    @Test
    public void shouldCreateChallengeParticipationForUserAndChallenge() throws Exception {
        //given
        User user = new User("username");
        Challenge challenge = challengeService.createChallenge(user);

        //when
        ChallengeParticipation challengeParticipation =
                challengeService.participateInChallenge(challenge, user);

        //then
        assertTrue(challengeParticipation != null);
    }

    private final static class ChallengesRepositoryStub extends ChallengesRepository {

        @Override
        public Challenge createChallenge(User user) {
            return new Challenge(user);
        }
    }
}
