package services;

import domain.FacebookUser;
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

    public User createNewOrGetExistingUser(final FacebookUser user, final String profilePictureUrl) {
        return withTransaction(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {

                if (usersRepository.isUserExist(user.getId())) {
                    return usersRepository.getUser(user.getId());
                }

                return usersRepository.createUser(user, profilePictureUrl);
            }
        });
    }

    public User createNewOrGetExistingUser(final String username, final String firstName, final String lastName, final String profilePictureUrl) {
        return withTransaction(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {

                if (usersRepository.isUserExist(username)) {
                    return usersRepository.getUser(username);
                }

                return usersRepository.createUser(username, firstName, lastName, profilePictureUrl);
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
