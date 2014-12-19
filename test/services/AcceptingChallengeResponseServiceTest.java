package services;

import domain.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.stubs.ChallengesRepositoryStub;
import services.stubs.UserRepositoryStub;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class AcceptingChallengeResponseServiceTest {

    private final ChallengesRepository challengesRepository = new ChallengesRepositoryStub();
    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final ChallengeNotificationsService challengeNotificationService = mock(ChallengeNotificationsService.class);
    private final FacebookService facebookService = mock(FacebookService.class);

    private final ChallengeService challengeService = new ChallengeService(challengesRepository, new UserService(usersRepository),
            challengeNotificationService, facebookService);


    private final User challengeCreator = new User("creatorUsername");
    private final User challengeParticipator = new User("creatorUsername");
    private final Challenge challenge = new Challenge(challengeCreator, "challengeName", ChallengeCategory.ALL, 0);
    private final ChallengeParticipation challengeParticipation = new ChallengeParticipation(challenge, challengeParticipator);
    private final ChallengeResponse challengeResponse = new ChallengeResponse(challengeParticipation);

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

    @Test
    public void shouldThrowExceptionWhenTryToRefuseAlreadyDecidedResponse() throws Exception {
        decideOnResponseInAnyWay(challengeResponse);
        expectedException.expect(RuntimeException.class);
        challengeService.refuseChallengeResponse(challengeResponse);
    }

    private void decideOnResponseInAnyWay(ChallengeResponse challengeResponse) {
        challengeService.acceptChallengeResponse(challengeResponse);
    }
}
