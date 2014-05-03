package services;

import domain.User;
import play.db.jpa.JPA;
import play.libs.F;
import repositories.UsersRepository;

public class UserService {

    private final UsersRepository usersRepository;

    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User createNewOrGetExistingUser(final String username) {
        try {
            return JPA.withTransaction(new F.Function0<User>() {
                @Override
                public User apply() throws Throwable {

                    if(usersRepository.isUserExist(username)) {
                        return usersRepository.getUser(username);
                    }

                    return usersRepository.createUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public User createNewOrGetExistingUser(final String username, final String profilePictureUrl) {
        try {
            return JPA.withTransaction(new F.Function0<User>() {
                @Override
                public User apply() throws Throwable {

                    if(usersRepository.isUserExist(username)) {
                        return usersRepository.getUser(username);
                    }

                    return usersRepository.createUser(username, profilePictureUrl);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }


    /**
     * TO_DO This method has not been implemented yet.
     */
    public static User getCurrentUser(){
        return new User("currentUser");
    }

}
