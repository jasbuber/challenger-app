package services;

import domain.User;
import org.junit.Test;
import repositories.UsersRepository;
import services.stubs.UserRepositoryStub;

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

}
