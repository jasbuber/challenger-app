package domain;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by jasbuber on 2014-12-22.
 */
public class ChallengeParticipationTests {

    private static User creator = new User("creator");

    private static User participator = new User("participator");

    private static Challenge challenge = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 0);

    @Test
    public void testEquals(){

        Challenge challenge2 = new Challenge(creator, "Test challenge2", ChallengeCategory.ALL, 0);
        User participator2 = new User("participator2");

        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        ChallengeParticipation participationDiffChallenge = new ChallengeParticipation(challenge2, participator);
        ChallengeParticipation participationDiffParticipator = new ChallengeParticipation(challenge, participator2);
        ChallengeParticipation participationEquals = participation;

        assertTrue(participation.equals(participationEquals));
        assertFalse(participation.equals(null));
        assertFalse(participation.equals(challenge));
        assertFalse(participation.equals(participationDiffChallenge));
        assertFalse(participation.equals(participationDiffParticipator));
    }

    @Test
    public void testGetJoined(){
        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        assertTrue(participation.getJoined().equals(new SimpleDateFormat("dd-MM-yyyy").format(new Date())));
    }

    @Test
    public void testRateChallenge(){
        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        assertFalse(participation.isChallengeRated());
        participation.rateChallenge();
        assertTrue(participation.isChallengeRated());
    }

    @Test
    public void testIsSubmitted(){
        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        assertFalse(participation.isResponseSubmitted());
        participation.submit();
        assertTrue(participation.isResponseSubmitted());
    }

    @Test
    public void testGetCreator(){
        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        assertTrue(participation.getCreator().equals(creator));
    }

    @Test
    public void testChallengeResponseGetSubmitted(){
        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        ChallengeResponse response = new ChallengeResponse(participation);
        assertTrue(response.getSubmitted().equals(new SimpleDateFormat("H:mm dd-MM-yyyy").format(new Date())));
    }

    @Test
    public void testCreateChallengeResponseWithVideoId(){
        String videoId = "some video id";
        ChallengeParticipation participation = new ChallengeParticipation(challenge, participator);
        ChallengeResponse response = new ChallengeResponse(participation, videoId);
        assertTrue(response.getVideoResponseUrl().equals(videoId));
    }

}
