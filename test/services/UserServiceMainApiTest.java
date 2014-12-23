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

    @Test
    public void shouldRewardUserWithCreationPoints() {
        usersRepository.createUser(EXISTING_USER_USERNAME);
        userService.rewardCreationPoints(EXISTING_USER_USERNAME, 5);

        User user = userService.getExistingUser(EXISTING_USER_USERNAME);
        assertEquals(5, user.getCreationPoints().intValue());
        assertEquals(5, user.getAllPoints().intValue());
    }

    @Test
    public void shouldRewardUserWithParticipationPoints() {
        usersRepository.createUser(EXISTING_USER_USERNAME);
        userService.rewardParticipationPoints(EXISTING_USER_USERNAME, 5);

        User user = userService.getExistingUser(EXISTING_USER_USERNAME);
        assertEquals(5, user.getParticipationPoints().intValue());
        assertEquals(5, user.getAllPoints().intValue());
    }

    @Test
    public void shouldAllPointsBeSumOfRewardAndParticipationPoints() {
        usersRepository.createUser(EXISTING_USER_USERNAME);
        userService.rewardParticipationPoints(EXISTING_USER_USERNAME, 5);
        userService.rewardCreationPoints(EXISTING_USER_USERNAME, 5);

        User user = userService.getExistingUser(EXISTING_USER_USERNAME);
        assertEquals(5, user.getCreationPoints().intValue());
        assertEquals(5, user.getParticipationPoints().intValue());
        assertEquals(10, user.getAllPoints().intValue());
    }

    @Test
    public void shouldRateUsersByTheirAllPoints() {
        String userOneName = "userOne";
        String userTwoName = "userTwo";
        String userThreeName = "userThree";

        User userOne = usersRepository.createUser(userOneName);
        User userTwo = usersRepository.createUser(userTwoName);
        User userThree = usersRepository.createUser(userThreeName);

        userService.rewardCreationPoints(userOneName, 5);
        userService.rewardCreationPoints(userTwoName, 3);
        userService.rewardCreationPoints(userThreeName, 1);

        userService.rewardParticipationPoints(userThreeName, 8);

        List<User> topRatedUsers = userService.getTopRatedUsers();
        assertEquals(topRatedUsers, Arrays.asList(userThree, userOne, userTwo));
    }
}
