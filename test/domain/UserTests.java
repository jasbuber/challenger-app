package domain;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertTrue;

/**
 * Created by jasbuber on 2014-12-22.
 */
public class UserTests {

    @Test
    public void testFormattedName(){
        String username = "username";
        String firstName = "FirstName";
        String lastName = "LastName";
        String shortName = firstName + " " + lastName.substring(0,3);

        User noNameUser = new User(username);
        User fullNameUser = new User(username, "profilePhotoUrl", firstName, lastName);

        assertTrue(noNameUser.getFormattedName().equals(username));
        assertTrue(fullNameUser.getFormattedName().equals(shortName));
    }

    @Test
    public void testGetJoined(){
        User user = new User("username");
        assertTrue(user.getJoined().equals(new SimpleDateFormat("dd-MM-yyyy").format(new Date())));
    }

    @Test
    public void testGetAllPoints(){
        User user = new User("username");

        assertTrue(user.getAllPoints() == 0);

        checkAddPoints(user, -5, -5, -5, 0);
        checkAddPoints(user, 5, 5, 5, 15);
        checkAddPoints(user, User.SPECIAL_REWARD + 1, User.SPECIAL_REWARD + 1, User.SPECIAL_REWARD + 1, 15);
    }

    private void checkAddPoints(User user, int creationPoints, int participationPoints, int otherPoints,
                                int expectedPoints){
        user.addCreationPoints(creationPoints);
        user.addParticipationPoints(participationPoints);
        user.addOtherPoints(otherPoints);

        assertTrue(user.getAllPoints() == expectedPoints);
    }
}
