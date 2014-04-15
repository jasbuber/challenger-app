package repositories;

import domain.Challenge;
import domain.User;

import java.util.ArrayList;
import java.util.List;

public class UsersRepository {

    public User createUser(String username) {
        return new User(username);
    }

    public boolean isUserExist(String username) {
        return true;
    }

    public User getUser(String username) {
        return new User(username);
    }

    public List<User> getParticipatorsFor(Challenge challenge) {
        return new ArrayList<User>();
    }
}
