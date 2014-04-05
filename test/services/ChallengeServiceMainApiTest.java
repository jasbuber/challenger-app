package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ChallengeServiceMainApiTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final ChallengeService challengeService = new ChallengeService(challengesRepository);
    private final String challengeName = "challengeName";

    @Test
    public void shouldCreateNewChallengeForUser() throws Exception {
        //when
        Challenge challenge = challengeService.createChallenge(new User("username"), challengeName);

        //then
        assertTrue(challenge != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfUserTriesToCreateTwoChallangesWithSameName() throws Exception {
        //when
        challengeService.createChallenge(new User("username"), challengeName);
        challengeService.createChallenge(new User("username"), challengeName);

        //then throws exception
    }

    @Test
    public void shouldChallengeCreationBeTrueIfUserHasAlreadyCreatedChallengeWithSameName() throws Exception {
        //given
        User creator = new User("username");

        //when
        challengeService.createChallenge(creator, challengeName);
        boolean userCreatedChallengeWithThisName = challengeService.isUserCreatedChallengeWithName(challengeName, creator);

        //then
        assertTrue(userCreatedChallengeWithThisName);
    }

    @Test
    public void shouldChallengeCreationBeFalseIfUserCreatesChallengeWithNameForTheFirstTime() throws Exception {
        //given
        User creator = new User("username");

        //when
        boolean userCreatedChallengeWithThisName = challengeService.isUserCreatedChallengeWithName(challengeName, creator);

        //
        assertFalse(userCreatedChallengeWithThisName);
    }

    @Test
    public void shouldCreateChallengeParticipationForUserAndChallenge() throws Exception {
        //given
        User user = new User("username");
        Challenge challenge = challengeService.createChallenge(user, challengeName);

        //when
        ChallengeParticipation challengeParticipation =
                challengeService.participateInChallenge(challenge, user);

        //then
        assertTrue(challengeParticipation != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenTryingToParticipateAgainInSameChallenge() throws Exception {
        //given
        User user = new User("username");
        Challenge challenge = challengeService.createChallenge(user, challengeName);

        //when
        challengeService.participateInChallenge(challenge, user);
        challengeService.participateInChallenge(challenge, user);

        //then throw exception
    }

    @Test
    public void shouldUserParticipationBeTrueIfUserIsAlreadyParticipatingInChallenge() throws Exception {
        //given
        User user = new User("username");
        Challenge challenge = challengeService.createChallenge(user, challengeName);

        //when
        challengeService.participateInChallenge(challenge, user);
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertTrue(userParticipatingInChallenge);
    }

    @Test
    public void shouldUserParticipationBeFalseIfUserIsNotParticipatingInChallengeYet() throws Exception {
        //given
        User user = new User("username");
        Challenge challenge = challengeService.createChallenge(user, challengeName);

        //when
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertFalse(userParticipatingInChallenge);
    }

    private final static class ChallengesRepositoryStub extends ChallengesRepository {

        private User challengeCreator;
        private String challengeName;

        private Challenge challengeParticipatedIn;
        private User userWhichParticipates;

        @Override
        public Challenge createChallenge(User creator, String challengeName) {
            this.challengeCreator = creator;
            this.challengeName = challengeName;
            return super.createChallenge(creator, challengeName);
        }

        @Override
        public boolean isChallengeWithGivenNameExistsForUser(String challengeName, User creator) {
            return challengeName.equals(this.challengeName) && creator.equals(challengeCreator);
        }

        @Override
        public ChallengeParticipation createChallengeParticipation(Challenge challenge, User user) {
            this.challengeParticipatedIn = challenge;
            this.userWhichParticipates = user;
            return super.createChallengeParticipation(challenge, user);
        }

        @Override
        public boolean isUserParticipatingInChallenge(Challenge challenge, User participator) {
            return challenge.equals(challengeParticipatedIn) && participator.equals(userWhichParticipates);
        }
    }
}
