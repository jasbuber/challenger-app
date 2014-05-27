package services;

import domain.User;
import play.libs.F;
import repositories.UsersRepository;

public class UserService extends TransactionalBase {

    private final UsersRepository usersRepository;

    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User createNewOrGetExistingUser(final String username) {
        return withTransaction(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {

                if (usersRepository.isUserExist(username)) {
                    return usersRepository.getUser(username);
                }

                return usersRepository.createUser(username);
            }
        });
    }

    public User createNewOrGetExistingUser(final String username, final String profilePictureUrl) {
        return withTransaction(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {

                if (usersRepository.isUserExist(username)) {
                    return usersRepository.getUser(username);
                }

                return usersRepository.createUser(username, profilePictureUrl);
            }
        });
    }


    /**
     * TO_DO This method has not been implemented yet.
     */
    public static User getCurrentUser() {
        return new User("currentUser");
    }

}
