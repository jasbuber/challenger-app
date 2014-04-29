package domain;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ChallengeEqualityTest {

    private final static ChallengeCategory ANY_CHALLENGE_CATEGORY = ChallengeCategory.ALL;

    @Test
    public void shouldChallengesBeEqualIfTheSameNameAndTheSameCreator() throws Exception {
        User creator = new User("username");
        Challenge challengeOne = new Challenge(creator, "challenge", ANY_CHALLENGE_CATEGORY);
        Challenge challengeTwo = new Challenge(creator, "challenge", ANY_CHALLENGE_CATEGORY);

        assertEquals(challengeOne, challengeTwo);
    }

    @Test
    public void shouldChallengesNotBeEqualIfTheSameNameButDifferentCreators() throws Exception {
        User creatorOne = new User("creatorOne");
        User creatorTwo = new User("creatorTwo");
        Challenge challengeOne = new Challenge(creatorOne, "challenge", ANY_CHALLENGE_CATEGORY);
        Challenge challengeTwo = new Challenge(creatorTwo, "challenge", ANY_CHALLENGE_CATEGORY);

        assertFalse(challengeOne.equals(challengeTwo));
    }

    @Test
    public void shouldChallengesNotBeEqualIfTheSameCreatorButDifferentNames() throws Exception {
        User creator = new User("creator");
        Challenge challengeOne = new Challenge(creator, "challengeOne", ANY_CHALLENGE_CATEGORY);
        Challenge challengeTwo = new Challenge(creator, "challengeTwo", ANY_CHALLENGE_CATEGORY);

        assertFalse(challengeOne.equals(challengeTwo));
    }

    @Test
    public void shouldChallengesNotBeEqualIfDifferentCreatorsAndNames() throws Exception {
        User creatorOne = new User("creatorOne");
        User creatorTwo = new User("creatorTwo");
        Challenge challengeOne = new Challenge(creatorOne, "challengeOne", ANY_CHALLENGE_CATEGORY);
        Challenge challengeTwo = new Challenge(creatorTwo, "challengeTwo", ANY_CHALLENGE_CATEGORY);

        assertFalse(challengeOne.equals(challengeTwo));
    }

    @Test
    public void shouldChallengesBeEqualIfDifferentNameCaseSensitivityOnly() throws Exception {
        User creator = new User("creator");
        Challenge challengeOne = new Challenge(creator, "challenge", ANY_CHALLENGE_CATEGORY);
        Challenge challengeTwo = new Challenge(creator, "CHALLENGE", ANY_CHALLENGE_CATEGORY);

        assertEquals(challengeOne, challengeTwo);
    }
}
