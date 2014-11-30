package services;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ChallengeServiceMainApiTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UsersRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = createChallengeService();

    private final String challengeName = "challengeName";

    private final static String SOME_VIDEO_ID = "videoId";
    private final Boolean VISIBILITY_PRIVATE = false;


    private ChallengeServiceWithoutTransactionMgmt createChallengeService() {
        return new ChallengeServiceWithoutTransactionMgmt(challengesRepository, usersRepository, challengeNotificationService);
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

    private Challenge createChallenge(String user) {
        return new Challenge(new User(user), challengeName, SOME_CATEGORY);
    }





    private final static class ChallengesRepositoryStub extends ChallengesRepository {

        private User challengeCreator;
        private String challengeName;

        private Challenge challengeParticipatedIn;
        private User userWhichParticipates;

        private Boolean visibility;

        @Override
        public Challenge createChallenge(Challenge challenge) {
            this.challengeCreator = challenge.getCreator();
            this.challengeName = challenge.getChallengeName();
            return challenge;
        }

        @Override
        public boolean isChallengeWithGivenNameExistsForUser(String challengeName, String creatorUsername) {
            return challengeName.equalsIgnoreCase(this.challengeName) && createUserStub(creatorUsername).equals(challengeCreator);
        }

        @Override
        public ChallengeParticipation persistChallengeParticipation(ChallengeParticipation challengeParticipation) {
            this.challengeParticipatedIn = challengeParticipation.getChallenge();
            this.userWhichParticipates = challengeParticipation.getParticipator();
            return challengeParticipation;
        }

        @Override
        public boolean isUserParticipatingInChallenge(Challenge challenge, String participator) {
            return challenge.equals(challengeParticipatedIn) && createUserStub(participator).equals(userWhichParticipates);
        }

        @Override
        public List<User> getAllParticipatorsOf(Challenge challenge) {
            return Collections.emptyList();
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
