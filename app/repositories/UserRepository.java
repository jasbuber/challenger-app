package repositories;

import domain.User;

public class UserRepository {

    public User createUser(String username) {
        return new User(username);
    }

    public boolean isUserExist(String username) {
        return true;
    }

    public User getUser(String username) {
        return new User(username);
    }
}
