package services;

import domain.*;
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
                true, new ArrayList<String>(), 0);
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
        usersRepository.createUser(user);
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

    /*
    @Test
    public void shouldNotNotifyUntilPopularityFactorIsAchieved() throws Exception {
        //when
        participateInChallenge("participator");

        //then
        verify(challengeNotificationService, never()).notifyAboutNewChallengeParticipation(challenge, "participator", Collections.<User>emptyList(), false);
    }

    @Test
    public void shouldNotifyOnlyParticipatorForWhomPopularityFactorIsAchieved() throws Exception {
        //when
        achieveOneFromPopularityFactor(challenge);
        participateInChallenge("participatorOfFactorAchievement");
        participateInChallenge("participatorAfterAchievement");

        //then
        verify(challengeNotificationService).notifyAboutNewChallengeParticipation(eq(challenge), eq("participatorOfFactorAchievement"), anyList(), true);
    }*/

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

    @Test(expected = IllegalStateException.class)
    public void testLeaveChallengeWhenNotParticipating(){
        User participator = usersRepository.createUser("participator");
        challengeService.leaveChallenge(challenge, "participator", "");
    }

    @Test(expected = IllegalStateException.class)
    public void testLeaveChallengeWhenAlreadySubmitted(){
        User participator = usersRepository.createUser("participator");

        challengeService.participateInChallenge(challenge, "participator", "");
        challengeService.submitChallengeResponse(challengeService.getChallengeParticipation(challenge, "participator"), "", "");
        challengeService.leaveChallenge(challenge, "participator", "");
    }

    @Test
    public void testLeaveChallenge(){
        User participator = usersRepository.createUser("participator");
        challengeService.participateInChallenge(challenge, "participator", "");
        assertTrue(challengeService.isUserParticipatingInChallenge(challenge, "participator"));
        challengeService.leaveChallenge(challenge, "participator", "");
        assertFalse(challengeService.isUserParticipatingInChallenge(challenge, "participator"));
    }

    /* Should be move to SubmittingChallengeResponseTest after clean up with old mocking system */
    @Test
    public void testAcceptResponse(){
        ChallengeResponse response = challengeService.acceptChallengeResponse(
                challengeService.getChallengeResponse(createChallengeResponse()));
        assertTrue(response.isAccepted());
    }

    @Test(expected = IllegalStateException.class)
    public void testTryToAcceptResponseMultipleTimes(){
        ChallengeResponse response = challengeService.acceptChallengeResponse(
                challengeService.getChallengeResponse(createChallengeResponse()));

        challengeService.acceptChallengeResponse(response);
    }

    @Test
    public void testRefuseResponse(){
        ChallengeResponse response = challengeService.refuseChallengeResponse(
                challengeService.getChallengeResponse(createChallengeResponse()));
        assertFalse(response.isAccepted());
    }

    @Test(expected = IllegalStateException.class)
    public void testTryToRefuseResponseMultipleTimes(){
        ChallengeResponse response = challengeService.refuseChallengeResponse(
                challengeService.getChallengeResponse(createChallengeResponse()));

        challengeService.refuseChallengeResponse(response);
    }

    private long createChallengeResponse(){

        User participator = usersRepository.createUser("participator");
        challengeService.participateInChallenge(challenge, "participator", "");

        return challengeService.submitChallengeResponse(
                challengeService.getChallengeParticipation(challenge, "participator"), "", "").getId();
    }

}
