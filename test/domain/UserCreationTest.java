package domain;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class UserCreationTest {

    @Test
    public void shouldCreateUserWithUsername() throws Exception {
        User user = new User("username");
        assertTrue(user != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTryingToCreateUserWithNoUsername() throws Exception {
        new User(null);
        //should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTryingToCreateUserWithEmptyUsername() throws Exception {
        new User("    ");
        //should throw exception
    }
}
