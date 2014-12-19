package services;

import domain.FacebookUser;
import domain.User;
import integration.EmTestsBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import repositories.UsersRepository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static class UserRepositoryStub extends UsersRepository {

        private Map<String, User> users = new HashMap<String, User>();

        @Override
        public User createUser(String username) {
            User newUser = new User(username);
            return addUser(newUser);
        }

        @Override
        public User createUser(FacebookUser fbUser, String profilePictureUrl) {
            User newUser = new User(fbUser, profilePictureUrl);
            return addUser(newUser);
        }

        @Override
        public User createUser(String username, String firstName, String lastName, String profilePictureUrl) {
            User newUser = new User(username, profilePictureUrl, firstName, lastName);
            return addUser(newUser);
        }

        private User addUser(User newUser) {
            users.put(newUser.getUsername(), newUser);
            return newUser;
        }

        @Override
        public boolean isUserExist(String username) {
            return users.containsKey(StringUtils.lowerCase(username));
        }

        @Override
        public User getUser(String username) {
            return users.get(StringUtils.lowerCase(username));
        }

        @Override
        public User rewardCreationPoints(String username, int points) {
            throw new NotImplementedException();
        }

        @Override
        public User rewardParticipationPoints(String username, int points) {
            throw new NotImplementedException();
        }

        @Override
        public User rewardOtherPoints(String username, int points) {
            throw new NotImplementedException();
        }

        @Override
        public List<User> getTopRatedUsers() {
            throw new NotImplementedException();
        }
    }
}
