package domain;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class ChallengeResponseAcceptanceTest {

    private static ChallengeParticipation ANY_CHALLENGE_PARTICIPATION = mock(ChallengeParticipation.class);

    @Test
    public void shouldBeMarkedAsAcceptedAfterAcceptance() throws Exception {
        //given
        ChallengeResponse challengeResponse = new ChallengeResponse(ANY_CHALLENGE_PARTICIPATION);

        //when
        challengeResponse.accept();

        //then
        assertTrue(challengeResponse.isAccepted());
    }

    @Test
    public void shouldBeMarkedAsNotAcceptedIfRefused() throws Exception {
        //given
        ChallengeResponse challengeResponse = new ChallengeResponse(ANY_CHALLENGE_PARTICIPATION);

        //when
        challengeResponse.refuse();

        //then
        assertFalse(challengeResponse.isAccepted());
    }

    @Test
    public void shouldBeMarkedAsNotAcceptedIfNoDecisionYet() throws Exception {
        //given
        ChallengeResponse challengeResponse = new ChallengeResponse(ANY_CHALLENGE_PARTICIPATION);

        //when

        //then
        assertFalse(challengeResponse.isAccepted());
    }

    @Test
    public void shouldBeMarkedAsNotDecidedYetIfNoDecision() throws Exception {
        //given
        ChallengeResponse challengeResponse = new ChallengeResponse(ANY_CHALLENGE_PARTICIPATION);

        //when

        //then
        assertFalse(challengeResponse.isDecided());
    }

    @Test
    public void shouldBeMarkedAsDecidedIfRefused() throws Exception {
        //given
        ChallengeResponse challengeResponse = new ChallengeResponse(ANY_CHALLENGE_PARTICIPATION);

        //when
        challengeResponse.refuse();

        //then
        assertTrue(challengeResponse.isDecided());
    }
}
