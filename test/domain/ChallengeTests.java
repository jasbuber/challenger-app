package domain;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertTrue;

/**
 * Created by jasbuber on 2014-12-21.
 */
public class ChallengeTests {

    private static User creator = new User("creator");

    @Test
    public void testSetInactive(){

        Challenge challenge = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 0);

        assertTrue("Ending date for new objects should be NULL", challenge.getEndingDate() == null);
        assertTrue("Challenge should be marked as active", challenge.isActive() == true);

        challenge.setInactive();

        assertTrue("Ending date for new objects shouldn't be NULL", challenge.getEndingDate() != null);
        assertTrue("Challenge should be marked as inactive", challenge.isActive() == false);
    }

    @Test
    public void testGetPopularityLevel(){
        assertTrue(Challenge.getPopularityLevel(-10L) == 1);
        assertTrue(Challenge.getPopularityLevel(0L) == 1);
        assertTrue(Challenge.getPopularityLevel((long)Challenge.POPULARITY_LEVEL_1) == 2);
        assertTrue(Challenge.getPopularityLevel((long)Challenge.POPULARITY_LEVEL_2) == 3);
        assertTrue(Challenge.getPopularityLevel((long)Challenge.POPULARITY_LEVEL_3) == 4);
        assertTrue(Challenge.getPopularityLevel((long)Challenge.POPULARITY_LEVEL_4) == 5);
        assertTrue(Challenge.getPopularityLevel((long)Challenge.POPULARITY_LEVEL_4 + 50) == 5);
    }

    @Test
    public void testFormattedDifficulty(){
        Challenge challenge = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, -1);
        Challenge challenge2 = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 0);
        Challenge challenge3 = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 1);
        Challenge challenge4 = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 2);
        Challenge challenge5 = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 3);
        Challenge challenge6 = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 4);

        assertTrue(challenge.getFormattedDifficulty().equals(Challenge.DifficultyLevel.easy.toString()));
        assertTrue(challenge2.getFormattedDifficulty().equals(Challenge.DifficultyLevel.easy.toString()));
        assertTrue(challenge3.getFormattedDifficulty().equals(Challenge.DifficultyLevel.medium.toString()));
        assertTrue(challenge4.getFormattedDifficulty().equals(Challenge.DifficultyLevel.hard.toString()));
        assertTrue(challenge5.getFormattedDifficulty().equals(Challenge.DifficultyLevel.special.toString()));
        assertTrue(challenge6.getFormattedDifficulty().equals(Challenge.DifficultyLevel.easy.toString()));
    }

    @Test
    public void testAddRating(){
        Challenge challenge = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 0);
        assertTrue(challenge.getRating() == 0);
        challenge.addRating(3);
        assertTrue(challenge.getRating() == 3.0);
        challenge.addRating(4);
        assertTrue(challenge.getRating() == 3.5);
        challenge.addRating(-5);
        challenge.addRating(0);
        challenge.addRating(50);
        assertTrue(challenge.getRating() == 3.5);
    }

    @Test
    public void testGetCreationDate(){
        Challenge challenge = new Challenge(creator, "Test challenge", ChallengeCategory.ALL, 0);
        assertTrue(challenge.getCreationDate().equals(new SimpleDateFormat("dd-MM-yyyy").format(new Date())));
    }


}
