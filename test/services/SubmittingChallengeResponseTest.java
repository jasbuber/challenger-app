package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.User;
import integration.EmTestsBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SubmittingChallengeResponseTest extends EmTestsBase {


    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = mock(UsersRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private final ChallengeService challengeService = new ChallengeService(challengesRepository, usersRepository, notificationService);

    private final String challengeName = "challengeName";
    private User creator = new User("creator");
    private User participator = new User("participator");
    private Challenge challenge = new Challenge(creator, challengeName);
    private ChallengeParticipation challengeParticipation = new ChallengeParticipation(challenge, participator);


    @Before
    public void setUp() {
        openTransaction();
    }

    @After
    public void tearDown() {
        closeTransaction();
    }

    @Test
    public void shouldSubmitChallengeResponseForChallengeParticipation() throws Exception {
        //given

        //when
        ChallengeResponse challengeResponse = challengeService.submitChallengeResponse(challengeParticipation);

        //then
        assertTrue(challengeResponse != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldDenySubmittingIfNotScoredResponseAlreadySubmittedForTheChallengeBySameUser() throws Exception {
        //given

        //when
        challengeService.submitChallengeResponse(challengeParticipation);
        challengeService.submitChallengeResponse(challengeParticipation);

        //then throw exception
    }


    @Test
    public void shouldSubmitChallengeResponseFromTwoDifferentParticipators() throws Exception {
        //given
        User participatorOne = new User("participatorOne");
        User participatorTwo = new User("participatorTwo");

        ChallengeParticipation challengeParticipationOne = new ChallengeParticipation(challenge, participatorOne);
        ChallengeParticipation challengeParticipationTwo = new ChallengeParticipation(challenge, participatorTwo);

        //when
        ChallengeResponse responseForParticipationOne = challengeService.submitChallengeResponse(challengeParticipationOne);
        ChallengeResponse responseForParticipationTwo = challengeService.submitChallengeResponse(challengeParticipationTwo);

        //then
        assertEquals(challengeParticipationOne, responseForParticipationOne.getChallengeParticipation());
        assertEquals(challengeParticipationTwo, responseForParticipationTwo.getChallengeParticipation());
    }

    @Test
    public void shouldSubmitChallengeResponseForTwoDifferentChallengesButSameParticipator() throws Exception {
        //given
        Challenge challengeOne = new Challenge(creator, "challengeOne");
        Challenge challengeTwo = new Challenge(creator, "challengeTwo");

        ChallengeParticipation challengeParticipationOne = new ChallengeParticipation(challengeOne, participator);
        ChallengeParticipation challengeParticipationTwo = new ChallengeParticipation(challengeTwo, participator);


        //when
        ChallengeResponse responseForParticipationOne = challengeService.submitChallengeResponse(challengeParticipationOne);
        ChallengeResponse responseForParticipationTwo = challengeService.submitChallengeResponse(challengeParticipationTwo);

        //then
        assertEquals(challengeParticipationOne, responseForParticipationOne.getChallengeParticipation());
        assertEquals(challengeParticipationTwo, responseForParticipationTwo.getChallengeParticipation());
    }

    @Test
    public void shouldNotifyChallengeCreatorWhenSubmittingForChallenge() throws Exception {
        //when
        challengeService.submitChallengeResponse(challengeParticipation);

        //then
        verify(notificationService).notifyUser(creator);
    }

    @Test
    public void shouldNotifyOtherChallengeParticipatorsWhenSubmittingForChallenge() throws Exception {
        //given
        User participatorOne = new User("participatorOne");
        User participatorTwo = new User("participatorTwo");

        ChallengeParticipation challengeParticipationOne = new ChallengeParticipation(challenge, participatorOne);

        given(usersRepository.getParticipatorsFor(challenge)).willReturn(Collections.singletonList(participatorTwo));

        //when
        challengeService.submitChallengeResponse(challengeParticipationOne);

        //then
        verify(notificationService).notifyUsers(Collections.singletonList(participatorTwo));
    }

    private class ChallengesRepositoryStub extends ChallengesRepository {

        private ChallengeResponse lastlyAddedChallengeResponse;

        @Override
        public boolean isNotScoredChallengeResponseExistsFor(ChallengeParticipation challengeParticipation) {
            return lastlyAddedChallengeResponse != null && challengeParticipation.equals(lastlyAddedChallengeResponse.getChallengeParticipation());
        }

        @Override
        public ChallengeResponse addChallengeResponse(ChallengeParticipation challengeParticipation) {
            ChallengeResponse challengeResponse = new ChallengeResponse(challengeParticipation);
            lastlyAddedChallengeResponse = challengeResponse;
            return challengeResponse;
        }
    }

}
