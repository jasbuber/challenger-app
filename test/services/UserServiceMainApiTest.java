package services;

import domain.User;
import org.junit.Test;
import repositories.UserRepository;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class UserServiceMainApiTest {

    private static final String EXISTING_USER_USERNAME = "existingUser";
    private static final User EXISTING_USER = new User(EXISTING_USER_USERNAME);

    private final UserRepository userRepository = new UserRepositoryStub();
    private final UserService userService = new UserService(userRepository);

    @Test
    public void shouldCreateNewUser() throws Exception {
        //given
        String username = "username";

        //when
        User newUser = userService.createNewOrGetExistingUser(username);

        //then
        assertTrue(!newUser.equals(EXISTING_USER));
    }

    @Test
    public void shouldNotCreateNewUserIfOneWithUsernameAlreadyExists() throws Exception {
        //when
        User returnedExistingUser = userService.createNewOrGetExistingUser(EXISTING_USER_USERNAME);

        //then
        assertEquals(EXISTING_USER, returnedExistingUser);
    }

    private static class UserRepositoryStub extends UserRepository {

        @Override
        public User createUser(String username) {
            return new User(username);
        }

        @Override
        public boolean isUserExist(String username) {
            return EXISTING_USER_USERNAME.equals(username);
        }

        @Override
        public User getUser(String username) {
            return EXISTING_USER;
        }
    }
}
