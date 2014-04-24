package services;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import integration.EmTestsBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ChallengeServiceMainApiTest extends EmTestsBase {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UsersRepositoryStub();
    private final NotificationService notificationService = mock(NotificationService.class);

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = new ChallengeService(challengesRepository, usersRepository, notificationService);
    private final String challengeName = "challengeName";

    @Before
    public void setUp() {
        openTransaction();
    }

    @After
    public void tearDown() {
        closeTransaction();
    }

    @Test
    public void shouldCreateNewChallengeForUser() throws Exception {
        //when
        Challenge challenge = createChallenge("username");

        //then
        assertTrue(challenge != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfUserTriesToCreateTwoChallangesWithSameName() throws Exception {
        //when
        createChallenge("username");
        createChallenge("username");

        //then throws exception
    }

    @Test
    public void shouldChallengeCreationBeTrueIfUserHasAlreadyCreatedChallengeWithSameName() throws Exception {
        //given
        String creator = "username";

        //when
        createChallenge(creator);
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
        Challenge challenge = createChallenge(user);

        //when
        ChallengeParticipation challengeParticipation =
                challengeService.participateInChallenge(challenge, user);

        //then
        assertTrue(challengeParticipation != null);
    }

    private Challenge createChallenge(String user) {
        return challengeService.createChallenge(user, challengeName, SOME_CATEGORY);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenTryingToParticipateAgainInSameChallenge() throws Exception {
        //given
        String user = "username";
        Challenge challenge = createChallenge(user);

        //when
        challengeService.participateInChallenge(challenge, user);
        challengeService.participateInChallenge(challenge, user);

        //then throw exception
    }

    @Test
    public void shouldUserParticipationBeTrueIfUserIsAlreadyParticipatingInChallenge() throws Exception {
        //given
        String user = "username";
        Challenge challenge = createChallenge(user);

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
        Challenge challenge = createChallenge(user);

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
        public Challenge createChallenge(User creator, String challengeName, ChallengeCategory category) {
            this.challengeCreator = creator;
            this.challengeName = challengeName;
            return new Challenge(creator, challengeName);
        }

        @Override
        public boolean isChallengeWithGivenNameExistsForUser(String challengeName, String creatorUsername) {
            return challengeName.equals(this.challengeName) && createUserStub(creatorUsername).equals(challengeCreator);
        }

        @Override
        public ChallengeParticipation createChallengeParticipation(Challenge challenge, User participator) {
            this.challengeParticipatedIn = challenge;
            this.userWhichParticipates = participator;
            return new ChallengeParticipation(challenge, participator);
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
