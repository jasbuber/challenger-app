package services;

import domain.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SubmittingChallengeResponseTest {

    private final static ChallengeCategory SOME_CHALLENGE_CATEGORY = ChallengeCategory.ALL;

    private final ChallengesRepository challengesRepository = mock(ChallengesRepository.class);
    private final UsersRepository usersRepository = mock(UsersRepository.class);

    private final NotificationService notificationService = mock(NotificationService.class);

    private final ChallengeService challengeService = new ChallengeServiceWithoutTransactionMgmt(challengesRepository, usersRepository, notificationService);
    private final String challengeName = "challengeName";
    private User creator = new User("creator");
    private User participator = new User("participator");
    private Challenge challenge = new Challenge(creator, challengeName, SOME_CHALLENGE_CATEGORY);
    private ChallengeParticipation challengeParticipation = new ChallengeParticipation(challenge, participator);

    @Before
    public void setUp() {
        when(challengesRepository.addChallengeResponse(Matchers.any(ChallengeResponse.class))).then(returnsFirstArg());
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
        given(challengesRepository.isNotEvaluatedChallengeResponseExistsFor(challengeParticipation)).willReturn(true);


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
        Challenge challengeOne = new Challenge(creator, "challengeOne", SOME_CHALLENGE_CATEGORY);
        Challenge challengeTwo = new Challenge(creator, "challengeTwo", SOME_CHALLENGE_CATEGORY);

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

        given(challengesRepository.getAllParticipatorsOf(challenge)).willReturn(Collections.singletonList(participatorTwo));

        //when
        challengeService.submitChallengeResponse(challengeParticipationOne);

        //then
        verify(notificationService).notifyUsers(Collections.singletonList(participatorTwo));
    }
}
