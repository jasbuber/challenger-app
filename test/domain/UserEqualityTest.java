package domain;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UserEqualityTest {

    @Test
    public void shouldUsersBeEqualIfTheSameUsername() throws Exception {
        User userOne = new User("username");
        User userTwo = new User("username");

        assertEquals(userOne, userTwo);
    }

    @Test
    public void shouldUsersNotBeEqualIfDifferentUsernames() throws Exception {
        User userOne = new User("userOne");
        User userTwo = new User("userTwo");

        assertFalse(userOne.equals(userTwo));
    }

    @Test
    public void usersShouldBeEqualIfDifferentCaseSensitivity() throws Exception {
        User userOne = new User("username");
        User userTwo = new User("USERNAME");
        assertEquals(userOne, userTwo);
    }

}
