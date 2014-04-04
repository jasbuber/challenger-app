package services;

import domain.User;
import repositories.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createNewOrGetExistingUser(String username) {
        if(userRepository.isUserExist(username)) {
            return userRepository.getUser(username);
        }

        return userRepository.createUser(username);
    }

}
