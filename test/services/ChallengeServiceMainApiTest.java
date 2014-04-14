package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ChallengeServiceMainApiTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UsersRepositoryStub();
    private final NotificationService notificationService = mock(NotificationService.class);

    private final ChallengeService challengeService = new ChallengeService(challengesRepository, usersRepository, notificationService);
    private final String challengeName = "challengeName";

    @Test
    public void shouldCreateNewChallengeForUser() throws Exception {
        //when
        Challenge challenge = challengeService.createChallenge("username", challengeName);

        //then
        assertTrue(challenge != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfUserTriesToCreateTwoChallangesWithSameName() throws Exception {
        //when
        challengeService.createChallenge("username", challengeName);
        challengeService.createChallenge("username", challengeName);

        //then throws exception
    }

    @Test
    public void shouldChallengeCreationBeTrueIfUserHasAlreadyCreatedChallengeWithSameName() throws Exception {
        //given
        String creator = "username";

        //when
        challengeService.createChallenge(creator, challengeName);
        boolean userCreatedChallengeWithThisName = challengeService.isUserCreatedChallengeWithName(challengeName, creator);

        //then
        assertTrue(userCreatedChallengeWithThisName);
    }

    @Test
    public void shouldChallengeCreationBeFalseIfUserCreatesChallengeWithNameForTheFirstTime() throws Exception {
        //given
        String creator = "username";

        //when
        boolean userCreatedChallengeWithThisName = challengeService.isUserCreatedChallengeWithName(challengeName, creator);

        //
        assertFalse(userCreatedChallengeWithThisName);
    }

    @Test
    public void shouldCreateChallengeParticipationForUserAndChallenge() throws Exception {
        //given
        String user = "username";
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
        String user = "username";
        Challenge challenge = challengeService.createChallenge(user, challengeName);

        //when
        challengeService.participateInChallenge(challenge, user);
        challengeService.participateInChallenge(challenge, user);

        //then throw exception
    }

    @Test
    public void shouldUserParticipationBeTrueIfUserIsAlreadyParticipatingInChallenge() throws Exception {
        //given
        String user = "username";
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
        String user = "username";
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
        public boolean isChallengeWithGivenNameExistsForUser(String challengeName, String creator) {
            return challengeName.equals(this.challengeName) && createUserStub(creator).equals(challengeCreator);
        }

        @Override
        public ChallengeParticipation createChallengeParticipation(Challenge challenge, User user) {
            this.challengeParticipatedIn = challenge;
            this.userWhichParticipates = user;
            return super.createChallengeParticipation(challenge, user);
        }

        @Override
        public boolean isUserParticipatingInChallenge(Challenge challenge, String participator) {
            return challenge.equals(challengeParticipatedIn) && createUserStub(participator).equals(userWhichParticipates);
        }

        private User createUserStub(String username) {
            return new User(username);
        }
    }

    private final static class UsersRepositoryStub extends UsersRepository {

        @Override
        public User getUser(String username) {
            return new User(username);
        }
    }
}
