package domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChallengeCreationTest {

    private final User creator = new User("creator");

    @Test
    public void shouldCreateChallenge() throws Exception {
        Challenge challenge = createChallengeWithName("challengeName");
        assertTrue(challenge != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoCreator() throws Exception {
        new Challenge(null, "challengeName", ChallengeCategory.ALL);
        //throws exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoChallengeName() throws Exception {
        createChallengeWithName(null);
        //throws exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfChallengeNameIsEmpty() throws Exception {
        createChallengeWithName("     ");
        //throws exception
    }

    @Test
    public void shouldChallengeNameBeLowerCase() throws Exception {
        Challenge challenge = createChallengeWithName("CHALLENGENAME");
        assertEquals("challengename", challenge.getChallengeName());
    }

    private Challenge createChallengeWithName(String challengeName) {
        return new Challenge(creator, challengeName, ChallengeCategory.ALL);
    }
}
