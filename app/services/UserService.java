package services;

import domain.User;
import repositories.UsersRepository;

public class UserService {

    private final UsersRepository usersRepository;

    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User createNewOrGetExistingUser(String username) {
        if(usersRepository.isUserExist(username)) {
            return usersRepository.getUser(username);
        }

        return usersRepository.createUser(username);
    }

    public static User getCurrentUser(){
        return new User("currentUser");
    }

}
