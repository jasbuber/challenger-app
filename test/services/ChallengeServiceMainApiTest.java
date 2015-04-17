package services;

import domain.Challenge;
import domain.ChallengeCategory;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.stubs.ChallengesRepositoryStub;
import services.stubs.UserRepositoryStub;

import java.util.Arrays;
import java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ChallengeServiceMainApiTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = createChallengeService();

    private final String challengeName = "challengeName";


    private ChallengeService createChallengeService() {
        return new ChallengeService(challengesRepository, new UserService(usersRepository), challengeNotificationService);
    }

    @Test
    public void shouldCreateNewChallengeForUser() throws Exception {
        //when
        Challenge challenge = createChallenge("username");

        //then
        assertTrue(challenge != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePrivateChallengeWithoutParticipants(){
        usersRepository.createUser("username");
        challengeService.createChallenge("username", challengeName, SOME_CATEGORY, false, Collections.<String>emptyList(), 0);
    }

    @Test
    public void testCreatePrivateChallenge(){
        usersRepository.createUser("username");
        usersRepository.createUser("participant1");
        usersRepository.createUser("participant2");
        usersRepository.createUser("notParticipant");

        Challenge challenge = challengeService.createChallenge("username", challengeName, SOME_CATEGORY, false,
                Arrays.asList(new String[]{"participant1,Guy,One,photo1","participant2,Guy,Two,photo2"}), 0);

        assertTrue(challengeService.isUserParticipatingInChallenge(challenge, "participant1"));
        assertTrue(challengeService.isUserParticipatingInChallenge(challenge, "participant1"));
        assertFalse(challengeService.isUserParticipatingInChallenge(challenge, "notParticipant"));
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
        return challengeService.createChallenge(user, challengeName, SOME_CATEGORY, true, Collections.<String>emptyList(), 0);
    }

}
