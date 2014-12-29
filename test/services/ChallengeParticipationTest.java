package services;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.User;
import org.junit.Before;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.stubs.ChallengesRepositoryStub;
import services.stubs.UserRepositoryStub;

import java.util.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ChallengeParticipationTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);
    private final VideoUploadingStrategy videoUploadingStrategy = mock(VideoUploadingStrategy.class);

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = createChallengeService();

    private final String challengeName = "challengeName";
    private final String user = "username";
    private User challengeCreator;

    private Challenge challenge;

    private ChallengeService createChallengeService() {
        return new ChallengeService(challengesRepository, new UserService(usersRepository), challengeNotificationService);
    }


    @Before
    public void setUp() {
        this.challengeCreator = usersRepository.createUser(user);
        this.challenge = challengeService.createChallenge(challengeCreator.getUsername(), challengeName, SOME_CATEGORY,
                true, new ArrayList<String>(), null, null, 0, videoUploadingStrategy);
    }

    @Test
    public void shouldUserParticipationBeTrueIfUserIsAlreadyParticipatingInChallenge() throws Exception {
        //when
        participateInChallenge(user);
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertTrue(userParticipatingInChallenge);
    }

    @Test
    public void shouldUserParticipationBeFalseIfUserIsNotParticipatingInChallengeYet() throws Exception {
        //when
        boolean userParticipatingInChallenge = challengeService.isUserParticipatingInChallenge(challenge, user);

        //
        assertFalse(userParticipatingInChallenge);
    }

    @Test
    public void shouldCreateChallengeParticipationForUserAndChallenge() throws Exception {
        //when
        ChallengeParticipation challengeParticipation =
                challengeService.participateInChallenge(challenge, user, user);

        //then
        assertTrue(challengeParticipation != null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenTryingToParticipateAgainInSameChallenge() throws Exception {
        //when
        participateInChallenge(user);
        participateInChallenge(user);

        //then throw exception
    }

    @Test
    public void shouldNotNotifyUntilPopularityFactorIsAchieved() throws Exception {
        //when
        participateInChallenge("participator");

        //then
        verify(challengeNotificationService, never()).notifyAboutNewChallengeParticipation(challenge, "participator", "participator", Collections.<User>emptyList());
    }

    @Test
    public void shouldNotifyOnlyParticipatorForWhomPopularityFactorIsAchieved() throws Exception {
        //when
        achieveOneFromPopularityFactor(challenge);
        participateInChallenge("participatorOfFactorAchievement");
        participateInChallenge("participatorAfterAchievement");

        //then
        verify(challengeNotificationService).notifyAboutNewChallengeParticipation(eq(challenge), eq("participatorOfFactorAchievement"), eq("participatorOfFactorAchievement"), anyList());
    }

    private void participateInChallenge(String username) {
        usersRepository.createUser(username);
        challengeService.participateInChallenge(challenge, username, username);
    }

    private void achieveOneFromPopularityFactor(Challenge challenge) {
        for (int i = 0; i < ChallengeService.POPULARITY_INDICATOR - 1; i++) {
            usersRepository.createUser("participator" + i);
            challengeService.participateInChallenge(challenge, "participator" + i, "participator" + i);
        }
    }
}
