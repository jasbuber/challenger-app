package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import static org.junit.Assert.assertTrue;

public class SubmitingChallengeResponseTest {


    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UsersRepositoryStub();
    private final ChallengeService challengeService = new ChallengeService(challengesRepository, usersRepository);
    private final String challengeName = "challengeName";

    @Test
    public void shouldSubmitChallengeResponseForChallengeParticipation() throws Exception {
        //given
        User creator = new User("creator");
        User participator = new User("participator");
        Challenge challenge = new Challenge(creator, challengeName);
        ChallengeParticipation challengeParticipation = new ChallengeParticipation(challenge, participator);

        //when
        ChallengeResponse challengeResponse = challengeService.submitChallengeResponse(challengeParticipation);

        //then
        assertTrue(challengeResponse != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldDenySubmittingIfNotScoredResponseAlreadySubmittedForTheChallengeBySameUser() throws Exception {
        //given
        User creator = new User("creator");
        User participator = new User("participator");
        Challenge challenge = new Challenge(creator, challengeName);
        ChallengeParticipation challengeParticipation = new ChallengeParticipation(challenge, participator);

        //when
        challengeService.submitChallengeResponse(challengeParticipation);
        challengeService.submitChallengeResponse(challengeParticipation);

        //then throw exception
    }

    private class ChallengesRepositoryStub extends ChallengesRepository {

        private ChallengeResponse lastlyAddedChallengeResponse;

        @Override
        public boolean isNotScoredChallengeResponseExistsFor(ChallengeParticipation challengeParticipation) {
            return lastlyAddedChallengeResponse != null && challengeParticipation.equals(lastlyAddedChallengeResponse.getChallengeParticipation());
        }

        @Override
        public ChallengeResponse addChallengeResponse(ChallengeParticipation challengeParticipation) {
            ChallengeResponse challengeResponse = super.addChallengeResponse(challengeParticipation);
            lastlyAddedChallengeResponse = challengeResponse;
            return challengeResponse;
        }
    }

    private class UsersRepositoryStub extends UsersRepository {
    }
}
