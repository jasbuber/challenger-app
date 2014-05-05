package services;

import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import integration.EmTestsBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class AcceptingChallengeResponseServiceTest {

    private final ChallengesRepository challengesRepository = mock(ChallengesRepository.class);
    private final UsersRepository usersRepository = mock(UsersRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private final static ChallengeCategory SOME_CATEGORY = ChallengeCategory.ALL;

    private final ChallengeService challengeService = new ChallengeServiceWithoutTransactionMgmt(challengesRepository, usersRepository, notificationService);
    private final String challengeName = "challengeName";

    private static ChallengeParticipation ANY_CHALLENGE_PARTICIPATION = mock(ChallengeParticipation.class);
    private final ChallengeResponse challengeResponse = new ChallengeResponse(ANY_CHALLENGE_PARTICIPATION);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldAcceptChallengeResponse() throws Exception {
        ChallengeResponse acceptedChallengeResponse = challengeService.acceptChallengeResponse(challengeResponse);
        assertTrue(acceptedChallengeResponse.isAccepted());
    }

    @Test
    public void shouldRefuseChallengeResponse() throws Exception {
        ChallengeResponse refusedChallengeResponse = challengeService.refuseChallengeResponse(challengeResponse);
        assertFalse(refusedChallengeResponse.isAccepted());
    }

    @Test
    public void shouldThrowExceptionWhenTryToAcceptAlreadyDecidedResponse() throws Exception {
        decideOnResponseInAnyWay(challengeResponse);
        expectedException.expect(RuntimeException.class);
        challengeService.acceptChallengeResponse(challengeResponse);
    }

    private void decideOnResponseInAnyWay(ChallengeResponse challengeResponse) {
        challengeService.acceptChallengeResponse(challengeResponse);
    }

    @Test
    public void shouldThrowExceptionWhenTryToRefuseAlreadyDecidedResponse() throws Exception {
        decideOnResponseInAnyWay(challengeResponse);
        expectedException.expect(RuntimeException.class);
        challengeService.refuseChallengeResponse(challengeResponse);
    }
}
