package domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChallengeCreationTest {

    @Test
    public void shouldCreateChallenge() throws Exception {
        Challenge challenge = new Challenge(new User("creator"), "challengeName");
        assertTrue(challenge != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoCreator() throws Exception {
        new Challenge(null, "challengeName");
        //throws exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoChallengeName() throws Exception {
        new Challenge(new User("creator"), null);
        //throws exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfChallengeNameIsEmpty() throws Exception {
        new Challenge(new User("creator"), "     ");
        //throws exception
    }

    @Test
    public void shouldChallengeNameBeLowerCase() throws Exception {
        Challenge challenge = new Challenge(new User("creator"), "CHALLENGENAME");
        assertEquals("challengename", challenge.getChallengeName());
    }
}
