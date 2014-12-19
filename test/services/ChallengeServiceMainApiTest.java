package services;

import domain.Challenge;
import domain.ChallengeCategory;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.stubs.ChallengesRepositoryStub;
import services.stubs.UserRepositoryStub;

import java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ChallengeServiceMainApiTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);
    private final FacebookService facebookService = mock(FacebookService.class);
    private final VideoUploadingStrategy videoUploadingStrategy = mock(VideoUploadingStrategy.class);

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = createChallengeService();

    private final String challengeName = "challengeName";


    private ChallengeService createChallengeService() {
        return new ChallengeService(challengesRepository, new UserService(usersRepository), challengeNotificationService, facebookService);
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
        usersRepository.createUser(user);
        return challengeService.createChallenge(user, challengeName, SOME_CATEGORY, true, Collections.<String>emptyList(), null, null, 0, videoUploadingStrategy);
    }

}
