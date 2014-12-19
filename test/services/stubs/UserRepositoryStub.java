package services.stubs;

import domain.FacebookUser;
import domain.User;
import org.apache.commons.lang3.StringUtils;
import repositories.UsersRepository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepositoryStub extends UsersRepository {

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
