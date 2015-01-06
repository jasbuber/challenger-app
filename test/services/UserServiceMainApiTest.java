package services;

import domain.User;
import junit.framework.Assert;
import org.junit.Test;
import repositories.UsersRepository;
import services.stubs.UserRepositoryStub;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class UserServiceMainApiTest {

    private static final String EXISTING_USER_USERNAME = "existingUser";
    private static final User EXISTING_USER = new User(EXISTING_USER_USERNAME);

    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final UserService userService = new UserService(usersRepository);


    @Test
    public void shouldCreateNewUser() throws Exception {
        //given
        String username = "username";

        //when
        User newUser = userService.createNewOrGetExistingUser(username, null, null, null);

        //then
        assertTrue(!newUser.equals(EXISTING_USER));
    }

    @Test
    public void shouldNotCreateNewUserIfOneWithUsernameAlreadyExists() throws Exception {
        //when
        User returnedExistingUser = userService.createNewOrGetExistingUser(EXISTING_USER_USERNAME, null, null, null);

        //then
        assertEquals(EXISTING_USER, returnedExistingUser);
    }

    @Test
    public void shouldGetAlreadyExistingUser() throws Exception {
        usersRepository.createUser(EXISTING_USER_USERNAME);
        User existingUser = userService.getExistingUser(EXISTING_USER_USERNAME);
        assertEquals(EXISTING_USER, existingUser);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfExistingUserNotInRepository() throws Exception {
        User existingUser = userService.getExistingUser(EXISTING_USER_USERNAME);
        assertEquals(EXISTING_USER, existingUser);
    }

}
