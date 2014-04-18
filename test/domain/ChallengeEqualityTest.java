package domain;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ChallengeEqualityTest {

    @Test
    public void shouldChallengesBeEqualIfTheSameNameAndTheSameCreator() throws Exception {
        User creator = new User("username");
        Challenge challengeOne = new Challenge(creator, "challenge");
        Challenge challengeTwo = new Challenge(creator, "challenge");

        assertEquals(challengeOne, challengeTwo);
    }

    @Test
    public void shouldChallengesNotBeEqualIfTheSameNameButDifferentCreators() throws Exception {
        User creatorOne = new User("creatorOne");
        User creatorTwo = new User("creatorTwo");
        Challenge challengeOne = new Challenge(creatorOne, "challenge");
        Challenge challengeTwo = new Challenge(creatorTwo, "challenge");

        assertFalse(challengeOne.equals(challengeTwo));
    }

    @Test
    public void shouldChallengesNotBeEqualIfTheSameCreatorButDifferentNames() throws Exception {
        User creator = new User("creator");
        Challenge challengeOne = new Challenge(creator, "challengeOne");
        Challenge challengeTwo = new Challenge(creator, "challengeTwo");

        assertFalse(challengeOne.equals(challengeTwo));
    }

    @Test
    public void shouldChallengesNotBeEqualIfDifferentCreatorsAndNames() throws Exception {
        User creatorOne = new User("creatorOne");
        User creatorTwo = new User("creatorTwo");
        Challenge challengeOne = new Challenge(creatorOne, "challengeOne");
        Challenge challengeTwo = new Challenge(creatorTwo, "challengeTwo");

        assertFalse(challengeOne.equals(challengeTwo));
    }

    @Test
    public void shouldChallengesBeEqualIfDifferentNameCaseSensitivityOnly() throws Exception {
        User creator = new User("creator");
        Challenge challengeOne = new Challenge(creator, "challenge");
        Challenge challengeTwo = new Challenge(creator, "CHALLENGE");

        assertEquals(challengeOne, challengeTwo);
    }
}
