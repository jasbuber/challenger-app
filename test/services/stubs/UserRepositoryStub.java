package services.stubs;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import domain.FacebookUser;
import domain.User;
import org.apache.commons.lang3.StringUtils;
import repositories.UsersRepository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;
import java.util.*;

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
        User user = users.get(StringUtils.lowerCase(username));
        if(user == null) {
            throw new IllegalStateException("User not in repository");
        }
        return user;
    }

    @Override
    public User rewardCreationPoints(String username, int points) {
        User user = users.get(StringUtils.lowerCase(username));
        user.addCreationPoints(points);
        return user;
    }

    @Override
    public User rewardParticipationPoints(String username, int points) {
        User user = users.get(StringUtils.lowerCase(username));
        user.addParticipationPoints(points);
        return user;
    }

    @Override
    public User rewardOtherPoints(String username, int points) {
        throw new NotImplementedException();
    }

    @Override
    public List<User> getTopRatedUsers() {
        return Ordering.from(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getAllPoints().compareTo(o2.getAllPoints());
            }
        }).reverse().sortedCopy(users.values());
    }

}
